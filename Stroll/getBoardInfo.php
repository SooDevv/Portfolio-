<?php
require_once 'dbconnect.php';

function unistr_to_xnstr($str){
    return preg_replace('/\\\u([a-z0-9]{4})/i', "&#x\\1;", $str);
}
$bid = $_POST['bid'];

mysqli_set_charset($conn,"utf8");

$query = mysqli_query($conn,"SELECT * from board WHERE bid='$bid'");

$result = array();

while ($row= mysqli_fetch_array($query)) {

    array_push($result,
        array('bid'=>$row[0], 'title'=>$row[1], 'limit_p'=>$row[2], 'date'=>$row[3], 'writer'=>$row[4],'course_name'=>$row[5],'detail_name'=>$row[6]
        ));
}

$json = json_encode(array("result"=>$result));
echo $json;

mysqli_close($conn);