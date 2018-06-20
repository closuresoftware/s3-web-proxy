# s3proxy
A web server that uses Amazon S3 as backend, optimized to be used as a private maven repository.

## Introduction

s3proxy is a grails application that acts as a proxy for an S3 backend, allowing GET/POST/PUT operations over the content.

It's mainly intended as a front-end for an S3 backed maven private repository, and thus authentication
is currently mandatory. Though that will be softened in the next version to allow public
GET operations.

The authentication system is limited currently to standard HTTP basic authentication,
 so you should put SSL on it, possibly with an Apache server using mod_jk.
 
The server uses a local cache to minimize the traffic to S3.

## Configuration

The app is optimized to be used as a docker container (you can use the public
docker image), so all configuration options can be set (in order of precedence)
as a grails configuration option, or a matching system property, or an environment
variable.

Available configuration options:

* **aws.bucket** or env var **S3PROXY_AWS_BUCKET**

    this is a mandatory value, with the name of the AWS S3 bucket holding the files.
    
* **auth.provider** or env var **S3PROXY_AUTH_PROVIDER**

    This is the full class name for the UserAuthProvider to use for authenticating users.
    By default uses the built-in DefaultUserAuthProvider.
    Check the authentication section for details.

* **auth.file** or env var **S3PROXY_AUTH_FILE**

    this is a mandatory value when using the default auth provider, contains a valid URL to load an authentication
    file. Check the authentication section for details.

* **auth.realm** or env var **S3PROXY_AUTH_REALM**

    This is the HTTP basic authentication realm display name, by default S3 Proxy.
    
* **cache.dir** or env var **S3PROXY_CACHE_DIR**

    By default this points to /var/lib/s3proxy/cache, which is defined as a
    VOLUME in the official docker image. You should probably change this
    if you'll be running the app inside a windows machine.
    
* **cache.maxSize** or env var **S3PROXY_CACHE_MAX_SIZE**

    This is the max size of the local cache, by default 1Gb. The max size can be expressed
    as a byte value, or using a valid suffix (m for Mb or g for Gb). Default value is, accordingly,
    1g.

You must also set these environment variables in order for the AWS sdk to work (check AWS Java SDK documentation for details):

* **AWS_ACCESS_KEY_ID**

* **AWS_SECRET_KEY**

* **AWS_REGION**

## Authentication

The authentication system can be easily modified by provided you own implementation of the
UserAuthProvider interface, and possibly you own implementation of the User interface.

The built-in authentication provider, DefaultUserAuthProvider, uses a password file similar to
htpasswd or unix passwd, with a single line per user entry.

Each entry has the form:

    username:password-digest

Where password-digest is a secure hash (MD5) of the password coded in Base64 format.

The software.closure.s3proxy.S3ProxyTools class has all the necessary methods, and provides
a main method to run it from the command line and generate new auth lines.

The DefaultAuthUserProvider uses the auth.file / S3PROXY_AUTH_FILE configuration option
which must be a valid URL as supported by java.net.URL, such as file: or http: or https:.

It also supports an S3 url which has the following form:

    s3://bucket-name/file-key

## License

s3proxy is Open Source licensed under the Apache 2.0 license.

