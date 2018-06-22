class UrlMappings {

	static mappings = {
        "/"( controller: 'main', action: 'index' )
        "/index"( controller: 'main', action: 'index' )
        "/__user/$action?/$id?"( controller: 'user' )
        "/**/*.*"( controller: 'main', action: 'index', method: "GET" )
        "/**/*.*"( controller: 'main', action: 'index', method: "HEAD" )
        "/**/*.*"( controller: 'main', action: 'putFile', method: "PUT" )
        "/**/*.*"( controller: 'main', action: 'putFile', method: "POST" )
        "500"(view:'/error')
	}
}
