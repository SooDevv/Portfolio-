<?php
require_once('dbconnect.php');

$seq = $_POST['seq'];

function unistr_to_xnstr($str){
    return preg_replace('/\\\u([a-z0-9]{4})/i', "&#x\\1;", $str);
}

mysqli_set_charset($conn,"utf8");

$query = mysqli_query($conn, "SELECT user_board.bid as bid, board.title as title ,board.date as date 
FROM board, user_board WHERE user_board.bid=board.bid AND user_board.seq=$seq AND date>NOW() ORDER BY date ASC " );

$result = array();

while($row=mysqli_fetch_array($query)){
    array_push($result,
        array('bid'=>$row[0], 'title'=>$row[1], 'date'=>$row[2])
    );
}

$json = json_encode(array("result"=>$result));
echo $json;

mysqli_close($conn);