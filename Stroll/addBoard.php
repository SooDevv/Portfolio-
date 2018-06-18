<?php
  require_once 'dbconnect.php';

  $title = $_POST['title'];
  $limit_p = $_POST['limit'];
  $date = $_POST['date'];
  $writer = $_POST['writer'];
  $course_name = $_POST['course_name'];
  $detail_name = $_POST['detail_name'];

  mysqli_set_charset($conn,"utf8"); //들어갈떄 utf8
  $query= "INSERT into board(title, limit_p, date, writer,course_name,detail_name) values
            ('$title','$limit_p','$date','$writer','$course_name','$detail_name')";

  if(!mysqli_query($conn,$query)){
    die('error:'.mysqli_error());
  }

  if(!$query){
    echo "업로드 실패";
  }else{
      echo "게시물이 업로드 완료";
  }


mysqli_close($conn);

 ?>
