import software.closure.s3webproxy.S3WebProxyTools
import software.closure.tools.grails.AppTools

class BootStrap {

    def grailsApplication
    def authenticationService

    def init = { servletContext ->

        def passwordFileURL = S3WebProxyTools.passwordFileURL
        if( !passwordFileURL ) {
            log.error "password file URL is not defined!!!!"
            System.exit( 1 )
        }
        if( !S3WebProxyTools.bucketName ) {
            log.error "bucketName is not defined!!!"
            System.exit( 1 )
        }
        authenticationService.initializeProvider()

        log.info AppTools.logSectionMarker
        log.info "db.url                    : ${grailsApplication.config.dataSource.url}"
        log.info "server.url                : ${grailsApplication.config.grails.serverURL}"
        log.info "S3 bucket name            : ${S3WebProxyTools.bucketName}"
        log.info "Cache dir                 : ${S3WebProxyTools.cacheDir}"
        log.info "Authentication provider   : ${S3WebProxyTools.authProviderClassName}"
        log.info "Authentication realm      : ${S3WebProxyTools.authenticationRealm}"
        log.info "Password file             : ${S3WebProxyTools.passwordFileURL}"
        log.info "Max. Cache size           : ${S3WebProxyTools.maxCacheSize}"
        log.info AppTools.logSectionMarker

        log.info AppTools.getAppCycleLogMessage( "started" )
    }

    def destroy = {
        log.info AppTools.getAppCycleLogMessage( "stopped" )
    }
}
