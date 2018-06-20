grails.config.locations = [ "file:${userHome}/.grails/${appName}-config.properties",
                            "file:${userHome}/.grails/${appName}-config.groovy"]

def ENV_NAME = "${appName.toUpperCase()}_CONFIG"
if(!grails.config.locations || !(grails.config.locations instanceof List)) {
    grails.config.locations = []
}
if( System.getenv(ENV_NAME) ) {
    println "Including configuration file specified in environment: " + System.getenv(ENV_NAME)
    grails.config.location << "file:" + System.getenv(ENV_NAME)

}
else if(System.getProperty(ENV_NAME)) {
    println "Including configuration file specified on command line: " + System.getProperty(ENV_NAME);
    grails.config.location << "file:" + System.getProperty(ENV_NAME)

}
grails.config.locations.each {
    println "possible config location: $it"
}

grails.project.groupId = appName // change this to alter the default package name and Maven publishing destination

// The ACCEPT header will not be used for content negotiation for user agents containing the following strings (defaults to the 4 major rendering engines)
grails.mime.disable.accept.header.userAgents = ['Gecko', 'WebKit', 'Presto', 'Trident']
grails.mime.types = [ // the first one is the default format
    all:           '*/*', // 'all' maps to '*' or the first available format in withFormat
    atom:          'application/atom+xml',
    css:           'text/css',
    csv:           'text/csv',
    form:          'application/x-www-form-urlencoded',
    html:          ['text/html','application/xhtml+xml'],
    js:            'text/javascript',
    json:          ['application/json', 'text/json'],
    multipartForm: 'multipart/form-data',
    rss:           'application/rss+xml',
    text:          'text/plain',
    hal:           ['application/hal+json','application/hal+xml'],
    xml:           ['text/xml', 'application/xml'],
                      // added types
    pdf: "application/pdf",
    rtf: "application/rtf",
//      excel: "application/vnd.ms-excel",
    excel: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
    ods: "application/vnd.oasis.opendocument.spreadsheet",
    zip: "application/zip"

]

// URL Mapping Cache Max Size, defaults to 5000
//grails.urlmapping.cache.maxsize = 1000

// Legacy setting for codec used to encode data with ${}
grails.views.default.codec = "html"

// The default scope for controllers. May be prototype, session or singleton.
// If unspecified, controllers are prototype scoped.
grails.controllers.defaultScope = 'singleton'

// GSP settings
grails {
    views {
        gsp {
            encoding = 'UTF-8'
            htmlcodec = 'xml' // use xml escaping instead of HTML4 escaping
            codecs {
                expression = 'html' // escapes values inside ${}
                scriptlet = 'html' // escapes output from scriptlets in GSPs
                taglib = 'none' // escapes output from taglibs
                staticparts = 'none' // escapes output from static template parts
            }
        }
        // escapes all not-encoded output at final stage of outputting
        // filteringCodecForContentType.'text/html' = 'html'
    }
}


grails.converters.encoding = "UTF-8"
// scaffolding templates configuration
grails.scaffolding.templates.domainSuffix = 'Instance'

// Set to false to use the new Grails 1.2 JSONBuilder in the render method
grails.json.legacy.builder = false
// enabled native2ascii conversion of i18n properties files
grails.enable.native2ascii = true
// packages to include in Spring bean scanning
grails.spring.bean.packages = []
// whether to disable processing of multi part requests
grails.web.disable.multipart=false

// request parameters to mask when logging exceptions
grails.exceptionresolver.params.exclude = ['password']

// configure auto-caching of queries by default (if false you can cache individual queries with 'cache: true')
grails.hibernate.cache.queries = false

// configure passing transaction's read-only attribute to Hibernate session, queries and criterias
// set "singleSession = false" OSIV mode in hibernate configuration after enabling
grails.hibernate.pass.readonly = false
// configure passing read-only to OSIV session by default, requires "singleSession = false" OSIV mode
grails.hibernate.osiv.readonly = false

environments {
    development {
        grails.logging.jul.usebridge = true
        grails.serverURL="http://${currentMacOSXIpAddress}:8080/"
//        grails.plugin.springsecurity.debug.useFilter = true
        grails.app.context = "/"
    }
    test {
        grails.logging.jul.usebridge = true
        grails.serverURL="http://${currentMacOSXIpAddress}:8080/"
        grails.app.context = "/"
    }
    production {
        grails.logging.jul.usebridge = false
        grails.app.context = "/"
    }
}

// log4j configuration
log4j.main = {
    // Example of changing the log pattern for the default console appender:
    //
    //appenders {
    //    console name:'stdout', layout:pattern(conversionPattern: '%c{2} %m%n')
    //}
    appenders {
        console name:'stdout', layout:pattern(conversionPattern: '%d [%p] %c{2} %m%n')

        // To send all errors or bigger level messages in email via SMTPAppender
        // Careful. If your SMTP server goes down, you may run into a thread deadlock scenario.
//        System.setProperty 'mail.smtp.port', config.mail.error.port.toString()
//        System.setProperty 'mail.smtp.starttls.enable', config.mail.error.starttls.toString()
//        appender new SMTPAppender(name: 'smtp',
//                threshold: Level.ERROR,
//                layout: pattern(conversionPattern: '%d{[ dd.MM.yyyy HH:mm:ss.SSS]} [%t] %n%-5p %n%c %n%C %n %x %n %m%n'),  // %5p: %d{dd MMM yyyy, HH:mm:ss} - %m%n'
//                to: config.mail.error.to,
//                from: config.mail.error.from,
//                subject: config.mail.error.subject,
//                evaluatorClass: ErrorEmailThrottle.name,
//                SMTPHost: config.mail.error.server,
//                SMTPUsername: config.mail.error.username,
//                SMTPDebug: config.mail.error.debug.toString(),
//                SMTPPassword: config.mail.error.password,
//                SMTPProtocol: config.mail.error.protocol,
//                bufferSize: config.mail.error.bufferSize
//        )
    }

    root {
        info 'stdout'
        additivity = true
    }

    error  'org.codehaus.groovy.grails.web.servlet',        // controllers
            'org.codehaus.groovy.grails.web.pages',          // GSP
            'org.codehaus.groovy.grails.web.sitemesh',       // layouts
            'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
            'org.codehaus.groovy.grails.web.mapping',        // URL mapping
            'org.codehaus.groovy.grails.commons',            // core / classloading
            'org.codehaus.groovy.grails.plugins',            // plugins
            'org.codehaus.groovy.grails.orm.hibernate',      // hibernate integration
            'org.springframework',
            'org.hibernate',
            'net.sf.ehcache.hibernate'

    info    "grails.app"
    info    'grails.plugin.springsecurity.web.filter.DebugFilter'

    debug   'software.closure'
    debug   "grails.app.domain.software.closure"
    debug   "grails.app.controllers.software.closure"
    debug   "grails.app.services"
    debug   "services"

//    trace   "grails.app.controllers.one.metrica.rpc.RpcController"

    // enable this to see sql and parameter values
//    trace 'org.hibernate.type'
//    debug 'org.hibernate.SQL'
}
