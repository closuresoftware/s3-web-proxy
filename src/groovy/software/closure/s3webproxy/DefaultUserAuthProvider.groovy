package software.closure.s3webproxy

import com.amazonaws.AmazonClientException
import com.amazonaws.AmazonServiceException
import com.amazonaws.services.s3.model.PutObjectRequest
import grails.plugin.awssdk.AmazonWebService

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
import grails.util.Holders
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Basic auth provider that uses a plain file on a URL (that can be an s3 bucket), very much like passwd,
 * with users and digested passwords.<br/>
 * The url for an s3 object is: <code>s3://bucket-name/object-path</code>
 *
 * Date: 24/3/18
 * Time: 0:10
 * @author narciso@closure.software
 * @since 0.1
 */
class DefaultUserAuthProvider implements UserAuthProvider {

    final static Logger log = LoggerFactory.getLogger( DefaultUser )

    Map<String,DefaultUser> userMap = null
    SortedSet<DefaultUser>  users   = null

    @Override
    void init() throws ConfigurationException {
        loadUserMap()
    }

    @Override
    User authenticate( final String username, final String password )
            throws ConfigurationException, InvalidCredentialsException
    {
        if( userMap == null ) {
            loadUserMap()
        }
        User user = null
        if( username == S3WebProxyTools.adminUsername ) {
            if( password == S3WebProxyTools.adminPassword ) {
                user = new DefaultUser( username: username )
            }
        }
        else {
            user = userMap?.get( username )
            if( user ) {
                String hash = S3WebProxyTools.calculateMD5( password.getBytes( "UTF-8" ) )
                if( hash != user.password ) {
                    user = null
                }
            }
        }
        if( user ) {
            return user
        }
        else {
            throw new InvalidCredentialsException()
        }
    }

    @Override
    boolean supportsUpdate() throws ConfigurationException {
        String path = S3WebProxyTools.passwordFileURL
        return path.startsWith( "s3://" ) || path.startsWith( "file:" )
    }

    @Override
    void updateUser( final String username, final String password ) throws ConfigurationException, S3WebProxyException {
        if( supportsUpdate() ) {
            try {
                DefaultUser current = userMap[username]
                if( current ) {
                    String updatedPasswordHash = S3WebProxyTools.calculateMD5( password.getBytes( 'UTF-8' ) )
                    if( current.password != updatedPasswordHash ) {
                        current.password = updatedPasswordHash
                        updateFile()
                    }
                }
                else {
                    DefaultUser user = new DefaultUser(
                            username: username,
                            password: S3WebProxyTools.calculateMD5( password.getBytes( 'UTF-8' ) )
                    )
                    users.add( user )
                    userMap[ user.username ] = user
                    updateFile()
                }
            }
            catch( ConfigurationException e ) {
                throw e
            }
            catch( e ) {
                throw new S3WebProxyException( e )
            }
        }
    }

    @Override
    void removeUser( final String username ) throws ConfigurationException, S3WebProxyException {
        if( supportsUpdate() ) {
            try {
                def current = userMap[username]
                if( current ) {
                    users.remove( current )
                    userMap.remove( username )
                    updateFile()
                }
            }
            catch( ConfigurationException e ) {
                throw e
            }
            catch( e ) {
                throw new S3WebProxyException( e )
            }
        }
    }

    @Override
    User findUser( final String username ) throws ConfigurationException, S3WebProxyException {
        userMap[username]
    }

    private updateFile() throws IOException, AmazonClientException, AmazonServiceException {
        String path = S3WebProxyTools.passwordFileURL
        if( path.startsWith( "file:" ) ) {
            URL url = new URL( path )
            File file = new File( url.path )
            writeToFile( file )
        }
        else if( path.startsWith( "s3://" ) ) {
            File file = null
            try {
                file = File.createTempFile( "s3proxy", ".auth" )
                writeToFile( file )
                def parts = getS3URLParts( path )
                PutObjectRequest putObjectRequest = new PutObjectRequest( parts.bucket, parts.key, file )
                amazonWebService.s3.putObject( putObjectRequest )
            }
            finally {
                try {
                    if( file ) {
                        if( !file.delete() ) {
                            file.deleteOnExit()
                        }
                    }
                }
                catch( ignored ) {}
            }
        }
    }

    private void writeToFile( File file ) {
        file.withPrintWriter { writer ->
            for( User user : users ) {
                writer.println "${user.username}:${user.password}"
            }
        }
    }

    @Override
    Set<User> listUsers( final String username ) throws ConfigurationException, S3WebProxyException {
        return users
    }

    private loadUserMap() throws ConfigurationException {
        String path = S3WebProxyTools.passwordFileURL
        if( !path ) {
            log.error "No auth file defined, can't continue"
            throw new ConfigurationException( "No auth file defined, can't continue" )
        }
        InputStream inputStream = null
        try {
            if( path.startsWith( "s3://" ) ) {
                def parts = getS3URLParts( path )
                def s3Object = amazonWebService.s3.getObject( parts.bucket, parts.key )
                inputStream = s3Object.objectContent
            }
            else {
                URL url = new URL( path )
                inputStream = url.openStream()
            }
            userMap = [:]
            users = new TreeSet<DefaultUser>()
            for( String line : inputStream.readLines( "UTF-8" ) ) {
                if( !line.startsWith( "#" ) && line.trim().length() > 0 ) {
                    def parts = line.split( ":" )
                    if( parts.length != 2 ) {
                        throw new ConfigurationException( "Invalid auth file content: $line" )
                    }
                    def user = new DefaultUser( username: parts[0].trim(), password: parts[1].trim() )
                    userMap[ user.username ] = user
                    users << user
                }
            }
        }
        catch( e ) {
            throw new ConfigurationException( e )
        }
        finally {
            try { inputStream?.close() } catch( ignored ) {}
        }
    }

    private static Map<String,String> getS3URLParts( String url ) throws ConfigurationException {
        String path = url.substring( 5 )
        int slash = path.indexOf( "/" )
        if( slash > 0 ) {
            [ bucket: path.substring( 0, slash ), key: path.substring( slash + 1 ) ]
        }
        else {
            throw new ConfigurationException( "Malformed s3 url: ${url}" )
        }

    }

    private static AmazonWebService getAmazonWebService() {
        (AmazonWebService) Holders.grailsApplication.mainContext.getBean( "amazonWebService" )
    }
}
