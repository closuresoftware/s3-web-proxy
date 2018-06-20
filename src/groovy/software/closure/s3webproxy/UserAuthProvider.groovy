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

/**
 * Find and authenticate users.
 *
 * Date: 24/3/18
 * Time: 0:07
 * @author narciso@closure.software
 * @since 0.1
 */
interface UserAuthProvider {

    /**
     * Initialize auth provider: load files, open connections, etc
     * @throws ConfigurationException on errors
     */
    void init() throws ConfigurationException

    /**
     * Authenticate a user.
     *
     * @param username user name
     * @param password password
     * @return User object upon successful authentication
     * @throws ConfigurationException if the provider is not properly configured
     * @throws InvalidCredentialsException if the credentials are invalid
     */
    User authenticate( String username, String password )
            throws ConfigurationException, InvalidCredentialsException

    /**
     * If the supplier, with the current configuration, supports updating user information.
     *
     * @return true if so
     * @throws ConfigurationException if the provider is not properly configured
     */
    boolean supportsUpdate() throws ConfigurationException

    /**
     * Update / Create a user in the underlying system.
     *
     * @param username user
     * @param password password
     * @throws ConfigurationException if the provider is not properly configured
     * @throws S3WebProxyException on errors
     */
    void updateUser( String username, String password ) throws ConfigurationException, S3WebProxyException

    /**
     * Remove a user in the underlying system.
     *
     * @param username user
     * @throws ConfigurationException if the provider is not properly configured
     * @throws S3WebProxyException on errors
     */
    void removeUser( String username ) throws ConfigurationException, S3WebProxyException

    /**
     * Get a user from the underlying system.
     *
     * @param username user
     * @return User object if found, null if not
     * @throws ConfigurationException if the provider is not properly configured
     * @throws S3WebProxyException on errors
     */
    User findUser( String username ) throws ConfigurationException, S3WebProxyException

    /**
     * Get a list of users, possibly filtered by user name.
     *
     * @param username user name to filter by, null means all users
     * @return list of matching users, maybe an empty list, never null
     * @throws ConfigurationException if the provider is not properly configured
     * @throws S3WebProxyException on errors
     */
    Set<User> listUsers( String username ) throws ConfigurationException, S3WebProxyException
}