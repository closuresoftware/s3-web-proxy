package software.closure.s3webproxy

/*
 Copyright 2018 Closure Software S.L.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

import com.amazonaws.AmazonClientException
import com.amazonaws.AmazonServiceException
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.PutObjectRequest
import com.amazonaws.services.s3.model.PutObjectResult
import grails.transaction.Transactional

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Transactional
class CacheService {

    private static long _1MB = 1024 * 1024
    private static long _1GB = _1MB * 1024

    def amazonWebService

    def sendObject( String uri, HttpServletResponse response, boolean sendContent = true )
            throws IOException, AmazonServiceException, AmazonClientException
    {
        String bucket = S3WebProxyTools.bucketName
        if( amazonWebService.s3.doesObjectExist( bucket, uri ) ) {
            def metadata = amazonWebService.s3.getObjectMetadata( bucket, uri )
            response.addHeader( "Content-Type", metadata.contentType )
            response.addHeader( "Content-Length", Long.toString( metadata.contentLength ) )
            if( metadata.lastModified ) {
                response.addDateHeader( "Last-Modified", metadata.lastModified.time )
            }
            if( sendContent ) {
                File file = getLocalFile( uri )
                if( isCacheObjectValid( uri, metadata ) ) {
                    sendStream( file.newInputStream(), response.outputStream )
                    touch( uri )
                }
                else {
                    def object = amazonWebService.s3.getObject( bucket, uri )
                    addCacheElement( uri, metadata.contentLength, metadata.getETag() )
                    file.parentFile.mkdirs()
                    sendStream( object.objectContent, response.outputStream, file.newOutputStream() )
                }
            }
            else {
                response.sendError( 200, "OK" ) // this is a HEAD operation, just send 200 OK
            }
        }
        else {
            response.sendError( 404, "NOT FOUND" )
        }
    }

    def putObject( HttpServletRequest request ) throws S3WebProxyException {
        try {
            String bucket = S3WebProxyTools.bucketName
            String uri = request.forwardURI
            if( uri.startsWith( "/" ) ) {
                uri = uri.substring( 1 )
            }
            File file = getLocalFile( uri )
            if( !file.parentFile.exists() ) {
                if( !file.parentFile.mkdirs() ) {
                    throw new S3WebProxyException( "can't create local cache path for: ${file.absolutePath}")
                }
            }
            file.withOutputStream { writeStream( request.inputStream, it ) }
            PutObjectRequest putObjectRequest = new PutObjectRequest( bucket, uri, file )
            PutObjectResult result = amazonWebService.s3.putObject( putObjectRequest )
            addCacheElement( uri, file.length(), result.getETag() )
        }
        catch( e ) {
            throw new S3WebProxyException( e )
        }
    }

    @SuppressWarnings("GrMethodMayBeStatic")
    private touch( String uri ) {
        CacheElement cacheElement = CacheElement.findByUri( uri )
        cacheElement?.lastUsed = new Date()
        cacheElement?.save( flush: true )
    }

    @SuppressWarnings("GrMethodMayBeStatic")
    private addCacheElement( String uri, long size, String eTag ) {
        CacheElement cacheElement = CacheElement.findByUri( uri )
        if( !cacheElement ) {
            cacheElement = new CacheElement( uri: uri, fileSize: size, eTag: eTag )
        }
        else {
            cacheElement.fileSize   = size
            cacheElement.lastUsed   = new Date()
            cacheElement.eTag       = eTag
        }
        cacheElement.save( flush: true )
        def cal = new GregorianCalendar()
        cal.add( Calendar.SECOND, 15 )
        CacheCleanUpJob.schedule( cal.time )
    }

    private static sendStream( InputStream input, OutputStream outputStream, OutputStream secondary = null )
            throws IOException
    {
        try {
            writeStream( input, outputStream, secondary )
        }
        finally {
            try { input?.close() } catch( ignored ) {}
            try { secondary?.close() } catch( ignored ) {}
        }
    }

    private static writeStream( InputStream input, OutputStream outputStream, OutputStream secondary = null )
            throws IOException
    {
        byte[] buffer = new byte[50 * 1024]
        int count = 0
//        while( count > -1 && input.available() ) {
        while( count > -1 ) {
            count = input.read( buffer )
            if( count > 0 ) {
                outputStream.write( buffer, 0, count )
                secondary?.write( buffer, 0, count )
            }
        }
    }

    private static boolean isCacheObjectValid( String uri, ObjectMetadata metadata ) {
        CacheElement cacheElement = CacheElement.findByUri( uri )
        File file = getLocalFile( uri )

        cacheElement &&
                file.exists() &&
                file.length() == metadata.contentLength &&
                cacheElement.eTag == metadata.getETag()
    }

    private static File getLocalFile( String uri ) throws S3WebProxyException {
        new File( cacheRoot, uri )
    }

    def cleanUp() {
        try {
            long maxSize = getMaxSize()
            def totalSize = CacheElement.withCriteria {
                projections {
                    sum( "fileSize" )
                }
            }.get( 0 ) ?: 0L
            while( totalSize > maxSize ) {
                def list = CacheElement.withCriteria {
                    maxResults(100)
                    order( "lastUsed", "asc" )
                }
                for( def cacheElement : list ) {
                    File file = getLocalFile( cacheElement.uri )
                    if( file.exists() ) {
                        log.info "Removing cache element: ${file.absolutePath} (${file.length()} bytes)"
                        file.delete()
                        totalSize -= file.length()
                        cacheElement.delete()
                        if( totalSize < maxSize ) {
                            break
                        }
                    }
                    else {
                        log.warn "File for cache element does not exists: ${cacheElement.uri}"
                    }
                }
            }
        }
        catch( e ) {
            log.error e
        }
    }

    private static File getCacheRoot() {
        def root = new File( S3WebProxyTools.cacheDir )
        if( !root.exists() ) {
            if( !root.mkdirs() ) {
                throw new S3WebProxyException( "Can't create root folder for cache system at: ${root.absolutePath}")
            }
        }
        root
    }

    private static long getMaxSize() {
        String rawSize = S3WebProxyTools.maxCacheSize
        try {
            if( rawSize.endsWith( "g" ) ) {
                return Long.parseLong( rawSize.substring( 0, rawSize.length() - 1 ) ) * _1GB
            }
            else if( rawSize.endsWith( "m" ) ) {
                return  Long.parseLong( rawSize.substring( 0, rawSize.length() - 1 ) ) * _1MB
            }
            else {
                return  Long.parseLong( rawSize )
            }
        }
        catch( NumberFormatException ignored ) {
            return  2 * _1GB
        }
    }
}
