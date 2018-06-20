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
import grails.transaction.Transactional
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsWebRequest

@Transactional
class AuthenticationService {

    private UserAuthProvider _userAuthProvider

    /**
     * Initialize the configured authentication provider.
     * @throws ConfigurationException on errors
     */
    void initializeProvider() throws ConfigurationException {
        userAuthProvider.init()
    }

    /**
     * Authenticate a the user for the current request.
     *
     * @return User object upon successful authentication
     * @throws InvalidCredentialsException if the credentials are invalid
     * @throws ConfigurationException if the service is not properly configured
     * @throws S3WebProxyException if no request context is available
     */
    User authenticate() throws InvalidCredentialsException, ConfigurationException, S3WebProxyException {
        def request = GrailsWebRequest.lookup()
        if( request ) {
            return authorize( request.getHeader( "authorization" ) )
        }
        else {
            throw new S3WebProxyException( "No request context available" )
        }
    }

    /**
     * Given a Basic authorization header, parses username and password and the looks for it in the user database,
     * authorizing the user if the password matches.
     *
     * @param authorization Basic authorization header
     * @return username if authorized, null otherwise
     * @throws InvalidCredentialsException if credentials are invalid
     * @throws ConfigurationException if the service is not properly configured
     */
    User authorize( String authorization ) throws InvalidCredentialsException, ConfigurationException {
        def credentials = S3WebProxyTools.parseAuthorization( authorization )
        if( credentials ) {
            userAuthProvider.authenticate( credentials.username, credentials.password )
        }
        else {
            throw new InvalidCredentialsException()
        }
    }

    /**
     * Get a list of users, possibly filtered by user name.
     *
     * @param username user name to filter by, null means all users
     * @return list of matching users, maybe an empty list, never null
     * @throws ConfigurationException if the provider is not properly configured
     * @throws S3WebProxyException on errors
     */
    Set<User> listUsers( String username = null ) {
        userAuthProvider.listUsers( username )
    }

    /**
     * Update / Create a user in the underlying system.
     *
     * @param username user
     * @param password password
     * @throws ConfigurationException if the provider is not properly configured
     * @throws S3WebProxyException on errors
     */
    void updateUser( String username, String password ) throws ConfigurationException, S3WebProxyException {
        userAuthProvider.updateUser( username, password )
    }

    /**
     * Remove a user in the underlying system.
     *
     * @param username user
     * @throws ConfigurationException if the provider is not properly configured
     * @throws S3WebProxyException on errors
     */
    void removeUser( String username ) throws ConfigurationException, S3WebProxyException {
        userAuthProvider.removeUser( username )
    }

    /**
     * Get a user from the underlying system.
     *
     * @param username user
     * @return User object if found, null if not
     * @throws ConfigurationException if the provider is not properly configured
     * @throws S3WebProxyException on errors
     */
    User findUser( String username ) throws ConfigurationException, S3WebProxyException {
        userAuthProvider.findUser( username )
    }


    private getUserAuthProvider() throws ConfigurationException {
        try {
            if( !_userAuthProvider ) {
                String className = S3WebProxyTools.authProviderClassName
                Class clazz = Class.forName( className )
                _userAuthProvider = (UserAuthProvider) clazz.newInstance()
            }
            _userAuthProvider
        }
        catch( e ) {
            throw new ConfigurationException( e )
        }
    }
}
