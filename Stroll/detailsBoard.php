<?php
require_once('dbconnect.php');

$bid = $_POST['bid'];

function unistr_to_xnstr($str){
    return preg_replace('/\\\u([a-z0-9]{4})/i', "&#x\\1;", $str);
}

mysqli_set_charset($conn,"utf8");

$query = mysqli_query($conn, "SELECT user_board.bid as bid, user_board.seq as seq,users.name as name ,
users.imgPath as imgPath from users, user_board WHERE users.seq=user_board.seq and user_board.bid=$bid");

$result = array();

while($row=mysqli_fetch_array($query)){
    array_push($result,
        array('bid'=>$row[0], 'seq'=>$row[1], 'name'=>$row[2], 'imgPath'=>$row[3])
    );
}

$json = json_encode(array("result"=>$result));
echo $json;

mysqli_close($conn);