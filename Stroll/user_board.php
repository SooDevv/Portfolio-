<?php

require_once 'dbconnect.php';

$query = $_POST['query'];

mysqli_set_charset($conn,"utf8"); //들어갈떄 utf8

//if(!mysqli_query($conn,$query)){
//    die('error:'.mysqli_error());
//}

$result = mysqli_query($conn,$query);
$row = mysqli_fetch_row($result);


if(!$query){
    echo "query 실패";
}
echo "query 수정 완료";


if($row){
    echo "attendance";
}else{
    echo "absent";
}


mysqli_close($conn);