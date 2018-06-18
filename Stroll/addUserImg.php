<?php
require_once 'dbconnect.php';

$imgPath = $_POST['imgPath'];
$id = $_POST['id'];

mysqli_set_charset($conn,"utf8"); //들어갈떄 utf8
$query= "UPDATE users SET imgPath='$imgPath' where id='$id'";

if(!mysqli_query($conn,$query)){
    die('error:'.mysqli_error());
}

if(!$query){
    echo "업로드 실패";
}

echo "이미지 업로드 완료";

mysqli_close($conn);