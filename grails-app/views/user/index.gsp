<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css" integrity="sha384-Gn5384xqQ1aoWXA+058RXPxPg6fy4IWvTNh0E263XmFcJlSAwiGgFAW/dAiS6JXm" crossorigin="anonymous">    <title>User list</title>
</head>

<body>
<div class="container">
    <div class="row-fluid">
        <div class="col-lg-12 col-md-12 col-sm-12 col-xs-12">
            <h1>User list</h1>
        </div>
    </div>
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
        %{----}%
    %{--</g:if>--}%
    <div class="row-fluid">
        <div class="col-lg-12 col-md-12 col-sm-12 col-xs-12">
<g:if test="${list}">
                <table>
                    <thead>
                    <tr>
                        <th>User name</th>
                    </tr>
                    </thead>
                    <tbody>
                    <g:each in="${list}" var="${user}">
                        <tr>
                            <td><g:link action="show" id="${user.username}">${user.username}</g:link></td>
                        </tr>
                    </g:each>
                    </tbody>
                </table>
</g:if>
            <g:else>
                No users found.
            </g:else>
            </div>
        </div>
    </div>
</body>
</html>