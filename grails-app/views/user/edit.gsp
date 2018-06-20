<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css" integrity="sha384-Gn5384xqQ1aoWXA+058RXPxPg6fy4IWvTNh0E263XmFcJlSAwiGgFAW/dAiS6JXm" crossorigin="anonymous">
    <asset:stylesheet src="custom.css"/>
    <title>s3proxy - user edit</title>
</head>

<body>
    <div class="container main-div">
        %{--<g:if test="${flash.message}">--}%
            %{--<div class="row-fluid">--}%
                %{--<div class="col-lg-12 col-md-12 col-sm-12 col-xs-12">--}%
                    %{--<div class="alert alert-primary alert-dismissible fade show" role="alert">--}%
                        %{--${flash.message}--}%
                        %{--<button type="button" class="close" data-dismiss="alert" aria-label="Close">--}%
                            %{--<span aria-hidden="true">&times;</span>--}%
                        %{--</button>--}%
                    %{--</div>--}%
                %{--</div>--}%
            %{--</div>--}%
        %{--</g:if>--}%
        <div class="row-fluid">
            <div class="col-lg-12 col-md-12 col-sm-12 col-xs-12">
                <g:link action="index">Back to user list</g:link>
            </div>
        </div>
        <div class="row-fluid">
            <div class="col-lg-12 col-md-12 col-sm-12 col-xs-12 user-info">
                <g:form method="post" action="update">
                    <div class="form-group">
                        <label for="username">Username</label>
                        <input name="username" type="text" class="form-control" id="username" aria-describedby="usernameHelp" placeholder="Enter username" value="${user.username}">
                        <small id="usernameHelp" class="form-text text-muted">The username is the unique identifier of a user</small>
                    </div>
                    <div class="form-group">
                        <label for="password">Password</label>
                        <input name="password" type="text" class="form-control" id="password" placeholder="Password">
                    </div>
                    <button type="submit" class="btn btn-primary">Submit</button>
                </g:form>
            </div>
        </div>
    </div>
</body>
</html>