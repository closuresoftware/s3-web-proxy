<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css" integrity="sha384-Gn5384xqQ1aoWXA+058RXPxPg6fy4IWvTNh0E263XmFcJlSAwiGgFAW/dAiS6JXm" crossorigin="anonymous">
    <asset:stylesheet src="custom.css"/>
    <title>s3proxy - show user</title>
</head>

<body>
<div class="container-fluid main-div">
    <div class="row-fluid">
        <div class="col-lg-12 col-md-12 col-sm-12 col-xs-12">
            <g:link action="index">Back to user list</g:link>
        </div>
    </div>
    <div class="row-fluid">
        <div class="col-lg-12 col-md-12 col-sm-12 col-xs-12">
            <g:link action="edit" id="${user.username}" class="btn btn-primary">Edit</g:link>
            <g:link action="delete" id="${user.username}" class="btn btn-danger">Delete</g:link>
            <table class="user-info">
                <tr>
                    <th>Username:</th>
                    <td>${user.username}</td>
                </tr>
            </table>
        </div>
    </div>
</div>

</body>
</html>