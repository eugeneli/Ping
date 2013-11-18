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
		global $PDOdb;
		$this->db = $PDOdb;	

		$this->id = $data[self::ID];
		$this->creatorId = $data[self::CREATOR_ID];
		$this->createDate = $data[self::CREATE_DATE];
		$this->latitude = $data[self::LATITUDE];
		$this->longitude = $data[self::LONGITUDE];
		$this->hasImage = $data[self::HAS_IMG];
		$this->rating = $data[self::RATING];
		$this->message = $data[self::MESSAGE];
		$this->b64image = $data[self::B64IMAGE];

		$stmt = $this->db->prepare("INSERT INTO ". self::TABLE_NAME ."(id,creator_id,create_date,latitude,longitude,has_image,rating,message,b64image) VALUES(:id,:creatorID,:createDate,:lat,:lon,:hasimg,:rating,:msg,:img)");
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

		$stmt = $this->db->prepare("UPDATE ". self::TABLE_NAME ." SET ". self::RATING ."=:rating WHERE id=:id");
		$stmt->execute(array(':rating' => $this->rating, ':id' => $this->id));
		if($stmt->rowCount() == 0)
			return false;
		else return true;
	}
}
?>