<?php
require_once("User.class.php");

$u = new User();
if($u->register("awsssd", "sds"))
	echo "worked. I have: ". $u->getPings() ." pings";
else
	echo "no";

//$u->login("asd", "asd");
//echo $u->getPings();
?>