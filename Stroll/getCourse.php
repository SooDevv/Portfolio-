<?php

require_once 'dbconnect.php';

function unistr_to_xnstr($str){
    return preg_replace('/\\\u([a-z0-9]{4})/i', "&#x\\1;", $str);
}

mysqli_set_charset($conn,"utf8");

$query =mysqli_query($conn,"SELECT * FROM course");

$result = array();

while ($row= mysqli_fetch_array($query)) {
    array_push($result,
        array('no'=>$row[0], 'name'=>$row[1]
        ));
}

$json = json_encode(array("result"=>$result));
echo $json;

mysqli_close($conn);
