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

class UserController {

    static allowedMethods = [index: 'GET', update: [ "PUT", "POST" ] ]

    def authenticationService

    def index() {
        authorize { username ->
            [ list: authenticationService.listUsers( params.username?.toString() ) ]
        }
    }

    def show() {
        authorize { username ->
            if( params.id ) {
                User user = authenticationService.findUser( params.id.toString() )
                if( user ) {
                    [ user: user ]
                }
                else {
                    flash.message = "User not found"
                    render( view: "index" )
                }
            }
            else {
                flash.message = "User not found"
                render( view: "index" )
            }
        }
    }

    def edit() {
        authorize { username ->
            if( params.id ) {
                User user = authenticationService.findUser( params.id.toString() )
                if( user ) {
                    [ user: user ]
                }
                else {
                    flash.message = "User not found"
                    render( view: "index" )
                }
            }
            else {
                flash.message = "User not found"
                render( view: "index" )
            }
        }
    }

    def update() {
        authorize { username ->
            if( params.username && params.password ) {
                if( params.password ) {
                    authenticationService.updateUser( params.username.toString(), params.password.toString() )
                    flash.message = "User updated"
                    render( view: "show", model: [ user: authenticationService.findUser( params.username.toString() ) ] )
                }
                else {
                    flash.message = "Invalid password"
                    render( view: "edit", model: [ user: authenticationService.findUser( params.username.toString() ) ] )
                }
            }
            else {
                flash.message = "Invalid user name"
                render( view: "index" )
            }
        }
    }

    def delete() {
        authorize { username ->
            if( params.id && params.password ) {
                authenticationService.removeUser( params.id.toString() )
                flash.message = "User removed"
                render( view: "index" )
            }
            else {
                flash.message = "Invalid user name"
                render( view: "index" )
            }
        }
    }

    private authorize( Closure closure ) {
        try {
            def credentials = S3WebProxyTools.parseAuthorization( request.getHeader( "authorization" ) )
            if( credentials.username == S3WebProxyTools.adminUsername && credentials.password == S3WebProxyTools.adminPassword ) {
                log.info "[${request.method}] ${request.forwardURI} : ${request.contentLength > 0 ? request.contentLength : '<empty>'}"
                closure.call( credentials.username )
            }
            else {
                response.addHeader( "WWW-Authenticate", "Basic realm=\"${S3WebProxyTools.authenticationRealm}\" charset=\"UTF-8\"" )
                response.sendError( 401, "Unauthorized" )
            }
        }
        catch( e ) {
            log.error e
            response.sendError( 500, e.message )
        }
    }
}
