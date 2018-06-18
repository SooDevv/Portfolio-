<?php

require_once 'dbconnect.php';

$name = $_POST['name'];
$gender = $_POST['gender'];
$id = $_POST['id'];
$pwd = $_POST['pwd'];
$phone = $_POST['phone'];
$email = $_POST['email'];

mysqli_set_charset($conn,"utf8"); //들어갈떄 utf8
$query= "INSERT into users(name,gender,id,pwd,phone,email) values('$name','$gender','$id','$pwd','$phone','$email')";
if(!mysqli_query($conn,$query)){
    die('error:'.mysqli_error($conn));
}


echo "1 record added";
mysqli_close($conn);

