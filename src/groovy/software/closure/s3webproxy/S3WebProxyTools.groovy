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

import com.amazonaws.util.Base64
import grails.util.Holders

import javax.servlet.http.HttpServletRequest
import java.security.MessageDigest

/**
 * Utility methods.
 *
 * Date: 23/3/18
 * Time: 19:48
 * @author narciso@closure.software
 * @since 0.1
 */
class S3WebProxyTools {

    /**
     * Get the matching S3 URI for a given request.
     *
     * @param request the request
     * @return the matching S3 URI
     */
    static String getS3URIFromRequest( HttpServletRequest request ) {
        String uri = request.forwardURI
        if( uri.startsWith( "/" ) ) {
            uri = uri.substring( 1 )
        }
        return uri
    }

    /**
     * Get the bucket name to use as a backend.
     *
     * @return bucket name, or null if not defined.
     */
    static String getBucketName() {
        getConfigValue( "aws.bucket", "S3PROXY_AWS_BUCKET" )
    }

    /**
     * Get the authentication realm for basic http authorization challenge.
     *
     * @return the realm, default to "Maven Repository" if not defined.
     */
    static String getAuthenticationRealm() {
        getConfigValue( "auth.realm", "S3PROXY_AUTH_REALM", "S3 Proxy" )
    }

    static String getPasswordFileURL() {
        getConfigValue( "auth.file", "S3PROXY_AUTH_FILE" )
    }

    static String getCacheDir() {
        getConfigValue( "cache.dir", "S3PROXY_CACHE_DIR", "/var/lib/s3proxy/cache" )
    }

    static String getMaxCacheSize() {
        getConfigValue( "cache.maxSize", "S3PROXY_CACHE_MAX_SIZE", "1gb" ).toLowerCase()
    }

    static String getAuthProviderClassName() {
        getConfigValue( "auth.provider", "S3PROXY_AUTH_PROVIDER", DefaultUserAuthProvider.class.name )
    }

    static String getAdminUsername() {
        getConfigValue( "auth.admin.username", "S3PROXY_ADMIN_USERNAME", "admin" )
    }

    static String getAdminPassword() {
        getConfigValue( "auth.admin.password", "S3PROXY_ADMIN_PASSWORD", "admin" )
    }

    static String getConfigValue( String configPath, String envName, String defaultValue = null ) {
        def path = configPath.split( "[.]" )
        def value = Holders.config
        for( def element : path ) {
            value = value?."$element"
        }
        if( !value || (value instanceof ConfigObject) || value.toString().trim().length() == 0 ) {
            value = System.getProperty( configPath )
            if( !value ) {
                value = System.getenv( envName )
            }
        }
        value != null ? value : defaultValue
    }

    /**
     * Get a Base64 encoded MD5 digest of a byte array.
     * @param data byte array
     * @return Base64 encoded MD5 digest
     */
    static String calculateMD5( byte[] data ) {
        MessageDigest md = MessageDigest.getInstance("MD5")
        md.update( data )
        byte[] digest = md.digest()
        byte[] bytes = Base64.encode( digest )
        new String( bytes )
    }

    static void main( String[] args ) {
        String userName = askFor( "User name" )
        String password = askFor( "Password" )
        println "\nPassword line:\n"
        println "${userName}: ${calculateMD5( password.getBytes('UTF-8') )}"
    }

    private static String askFor( String prompt ) {
        print "${prompt}: "
        String answer = null
        def input = System.in.newReader()
        while( !answer ) {
            answer = input.readLine()
        }
        answer
    }

    /**
     * Parse a Basic authorization header, returning a map with username and password keys.
     *
     * @param authorization the content of a Basic authorization header
     * @return a map with username and password keys, or null if the header can't be parsed
     */
    static Map<String,String> parseAuthorization( String authorization ) {
        if( authorization && authorization.startsWith( "Basic " ) ) {
            byte[] decoded = Base64.decode( authorization.substring( 6 ) )
            String auth = new String( decoded, "UTF-8" )
            String[] parts = auth.split( ":" )
            if( parts.length == 2 ) {
                return [ username: parts[0], password: parts[1] ]
            }
        }
        null
    }
}
