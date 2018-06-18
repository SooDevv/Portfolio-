<?php
require_once('dbconnect.php');

$num = $_POST['num'];

function unistr_to_xnstr($str){
    return preg_replace('/\\\u([a-z0-9]{4})/i', "&#x\\1;", $str);
}

mysqli_set_charset($conn,"utf8");

$query = mysqli_query($conn, "Select course_detail.course_num as num, course.name as course_name, 
          course_detail.name as detail_name, course_detail.lat as lat, course_detail.lng as lng 
          from course,course_detail where course.no=course_detail.course_num+1 and course_detail.course_num=$num");

$result = array();

while($row=mysqli_fetch_array($query)){
    array_push($result,
        array('num'=>$row[0], 'course_name'=>$row[1], 'detail_name'=>$row[2], 'lat'=>$row[3], 'lng'=>$row[4])
        );
}

$json = json_encode(array("result"=>$result));
echo $json;

mysqli_close($conn);
