<?php
require_once("db.php");

class Ping
{
	const TABLE_NAME = "pings";
	const ID = "id";
	const CREATOR_ID = "creator_id";
	const CREATE_DATE = "create_date";
	const LATITUDE = "latitude";
	const LONGITUDE = "longitude";
	const HAS_IMG = "has_image";
	const RATING = "rating";
	const MESSAGE = "message";
	const B64IMAGE = "b64image";

	private $id;
	private $creatorId;
	private $createDate;
	private $latitude;
	private $longitude;
	private $hasImage;
	private $rating;
	private $message;
	private $b64Image;

	public function __construct($data)
	{
		$this->id = $data[ID];
		$this->creatorId = $data[CREATOR_ID];
		$this->createDate = $data[CREATE_DATE];
		$this->latitude = $data[LATITUDE];
		$this->longitude = $data[LONGITUDE];
		$this->hasImage = $data[HAS_IMG];
		$this->rating = $data[RATING];
		$this->message = $data[MESSAGE];
		$this->b64image = $data[B64IMAGE];

		$stmt = $db->prepare("INSERT INTO ". TABLE_NAME ."(id,creator_id,create_date,latitude,longitude,has_image,rating,message,b64image) VALUES(:id,:creatorID,:createDate,:lat,:lon,:hasimg,:rating,:msg,:img)");
		$stmt->execute(array(':id' => $this->id, ':creatorID' => $this->creatorId, ':createDate' => $this->createDate, ':lat' => $this->latitude, ':long' => $this->longitude, ':hasimg' => $this->hasImage, ':rating' => $this->rating, ':msg' => $this->message, ':img' => $this->b64image));
		if($stmt->rowCount() == 0)
			return false;
		else return true;
	}

	public function getCreatorId() { return $this->creatorId; }

	public function getRating() { return $this->rating; }
	public function setRating($newRating)
	{
		$this->rating = $newRating;

		$stmt = $db->prepare("UPDATE ". TABLE_NAME ." SET ". RATING ."=:rating WHERE id=:id");
		$stmt->execute(array(':rating' => $this->rating, ':id' => $this->id));
		if($stmt->rowCount() == 0)
			return false;
		else return true;
	}
}
?>