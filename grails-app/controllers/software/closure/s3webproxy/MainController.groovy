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


class MainController {

    static allowedMethods = [index: [ 'GET', 'HEAD' ], putFile: [ "PUT", "POST" ] ]

    def authenticationService
    def cacheService

    def index() {
        authorize { String user ->
            String uri = request.forwardURI
            if( uri == "/" || uri == "/is-tomcat-running" ) {
                render( text: "ok" )
            }
            else {
                String[] parts = uri.split( "/" )
                if( parts[parts.length - 1].startsWith( "." ) ) {
                    response.sendError( 403, "Forbidden" )
                }
                else {
                    uri = S3WebProxyTools.getS3URIFromRequest( request )
                    cacheService.sendObject( uri, response, request.method == 'GET' )
                }
            }
        }
    }

    def putFile() {
        authorize { String user ->
            cacheService.putObject( request )
            response.sendError( 200, "OK" )
        }
    }

    private authorize( Closure closure ) {
        try {
            log.info "[${request.method}] ${request.forwardURI} : ${request.contentLength > 0 ? request.contentLength : '<empty>'}"
            if( (request.method ==  'GET' || request.method == 'HEAD') && S3WebProxyTools.allowPublicGet() ) {
                closure.call( null )
            }
            else {
                closure.call( authenticationService.authenticate().username )
            }
        }
        catch( InvalidCredentialsException ignored ) {
            response.addHeader( "WWW-Authenticate", "Basic realm=\"${S3WebProxyTools.authenticationRealm}\" charset=\"UTF-8\"" )
            response.sendError( 401, "Unauthorized" )
        }
        catch( e ) {
            log.error e
            response.sendError( 500, e.message )
        }
    }
}
