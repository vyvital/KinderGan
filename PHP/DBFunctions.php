<?php

class DBFunctions
{
	
	private $conn;
	
	// constructor
	function __construct()
	{
		require_once 'DBConnect.php';
		// importing the necessary Cloudinary files to upload image to cloud
		require_once 'cloudinary/Uploader.php';
		
		// connecting to database
		$db = new DBConnect();
		$this->conn = $db->connect();
	}
	
	// destructor
	function __destruct()
	{
		
	}
	
	/**
     * Storing new teacher
     * returns user details
     */
	public function storeTeacher($id, $fname, $lname, $phone, $photo, 
								 $kindergan_name, $kindergan_city, $kindergan_class,
								 $email, $password)
	{
		$hash = $this->hashBCRYPT($password);
		
		// path of the image in the folder of nannies images with filename as the id of the nanny
		$path = "images/nannies/$id.jpg";
		$actualpath = "http://res.cloudinary.com/kindergan/image/upload/$path";
		
		$stmt = $this->conn->prepare("INSERT INTO teachers(ID, first_name, last_name, phone, photo, kindergan_name, kindergan_city, kindergan_class, email, encrypted_password, created_at)  VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())");
		$stmt->bind_param("sssssssiss", $id, $fname, $lname, $phone, $actualpath, 
										$kindergan_name, $kindergan_city, $kindergan_class,
										$email, $hash);
		$result = $stmt->execute();
		$stmt->close();
		
		// check for successful store
		if ($result)
		{
			$stmt = $this->conn->prepare("SELECT * FROM teachers WHERE email = ?");
			$stmt->bind_param("s", $email);	
			$stmt->execute();
			$user = $stmt->get_result()->fetch_assoc();
			// Add the image file to server
			file_put_contents($path,base64_decode($photo));
			// Upload the image from server to external cloud
			\Cloudinary\Uploader::upload("$path", array("public_id" => "images/nannies/$id", "invalidate" => true));
			// Remove the image file from server
			unlink($path);
			$stmt->close();
			
			return $user;
		}
		else
		{
			return false;
		}
	}
	
	/**
     * Storing new parent and kid
     * returns user details
     */
	public function storeParent($id, $fname, $lname, $address, $phone,
								$kid_name, $birthdate, $photo, $kindergan_name, $class,
								$email, $password)
	{
		$hash = $this->hashBCRYPT($password);
		
		// path of the image in the folder of parents images with filename as the id of the parent
		$path = "images/kids/$id.jpg";
		$actualpath = "http://res.cloudinary.com/kindergan/image/upload/$path";
		
		// insert into parents table
		$stmt = $this->conn->prepare("INSERT INTO parents(ID, first_name, last_name, address, phone, email, encrypted_password, created_at)  VALUES(?, ?, ?, ?, ?, ?, ?, NOW())");
		$stmt->bind_param("sssssss", $id, $fname, $lname, $address, $phone, $email, $hash);
		$result1 = $stmt->execute();
		$stmt->close();
		
		// insert into kids table
		$stmt = $this->conn->prepare("INSERT INTO kids(name, birth_date, photo, kindergan_name, class, parent_id, created_at)  VALUES(?, ?, ?, ?, ?, ?, NOW())");
		$stmt->bind_param("ssssis", $kid_name, $birthdate, $actualpath, $kindergan_name, $class, $id);
		$result2 = $stmt->execute();
		$stmt->close();
		
		// check for successful store
		if ($result2)
		{
			$stmt = $this->conn->prepare("SELECT * FROM kids,parents WHERE kids.parent_id = ? and parents.ID = ?");
			$stmt->bind_param("ss", $id, $id);
			$stmt->execute();
			$user = $stmt->get_result()->fetch_assoc();
			// Add the image file to server
			file_put_contents($path,base64_decode($photo));
			// Upload the image from server to external cloud
			\Cloudinary\Uploader::upload("$path", array("public_id" => "images/kids/$id", "invalidate" => true));
			// Remove the image file from server
			unlink($path);
			$stmt->close();
			
			return $user;
		}
		else
		{
			return false;
		}
	}
	
	/**
     * Storing new kid attendance
	 * returns kid details
     */
	public function storeAttendance($kindergan_name, $class, $parent_id, $kid_name)
	{		
		// insert into attendance table
		$stmt = $this->conn->prepare("INSERT INTO attendance(kindergan_name, class, parent_id, name) VALUES(?, ?, ?, ?)");
		$stmt->bind_param("siss", $kindergan_name, $class, $parent_id, $kid_name);
		$result = $stmt->execute();
		$stmt->close();
				
		// check for successful store
		if ($result)
		{
			$stmt = $this->conn->prepare("SELECT * FROM attendance WHERE parent_id = ?");
			$stmt->bind_param("s", $parent_id);
			$stmt->execute();
			$kid = $stmt->get_result()->fetch_assoc();
			$stmt->close();
			
			return $kid;
		}
		else
		{
			return false;
		}
	}
	
	/**
     * Get user by email and password
     */
	public function getUserByEmailAndPassword($email, $password, $type)
	{
		// Nanny
		if ($type == 1)
		{
			// prepare and bind
			$stmt = $this->conn->prepare("SELECT * FROM teachers WHERE email = ?");
			$stmt->bind_param("s", $email);	
		}
		// Parent
		else if ($type == 2)
		{
			// prepare and bind
			$stmt = $this->conn->prepare("SELECT * FROM parents,kids WHERE parents.email = ? and parents.ID = kids.parent_id");
			$stmt->bind_param("s", $email);	
		}
			
		if ($stmt->execute())
		{
			$user = $stmt->get_result()->fetch_assoc();
			$stmt->close();
			
			// verifying user password
			$hash = $user['encrypted_password'];
			if ($this->VerifyPassword($password, $hash)) 
			{
				// user authentication details are correct
				return $user;
			}
		}
		else
		{
			return Null;
		}
	}

	/**
     * Check teacher user is existed or not
     */
	public function isTeacherExisted($email)
	{
		// prepare and bind
		$stmt = $this->conn->prepare("SELECT email FROM teachers WHERE email = ?");
		$stmt->bind_param("s", $email);	
		$stmt->execute();
		$stmt->store_result();
		
		if ($stmt->num_rows > 0)
		{
			// user existed
			$stmt->close();
			return true;
		}
		else
		{
			// user not existed
			$stmt->close();
			return false;
		}
	}
	
	/**
     * Check parent user is existed or not
     */
	public function isParentExisted($email)
	{
		// prepare and bind
		$stmt = $this->conn->prepare("SELECT email FROM parents WHERE email = ?");
		$stmt->bind_param("s", $email);	
		$stmt->execute();
		$stmt->store_result();
		
		if ($stmt->num_rows > 0)
		{
			// user existed
			$stmt->close();
			return true;
		}
		else
		{
			// user not existed
			$stmt->close();
			return false;
		}
	}

	/**
     * Get all KinderGartens
     */
	public function GetAllKinderGans()
	{
		$stmt = $this->conn->prepare("SELECT * FROM kindergans");
		$stmt->execute();
		$result = $stmt->get_result();
		while ($row = $result->fetch_assoc())
		{
			$kindergans[] = $row;
		}
		$stmt->close();
		return $kindergans;
	}
		
	/**
     * Get required Teachers by parent id, kid kindergan name, kid kindergan class as parameters
     */
	public function GetRequiredTeachers($parent_id, $kindergan_name, $kindergan_class)
	{
		// prepare and bind
		$stmt = $this->conn->prepare("SELECT t.ID, t.first_name, t.last_name, t.phone, t.kindergan_name, t.kindergan_class, t.email, t.notification_time
								      FROM teachers t INNER JOIN kids k
									  ON t.kindergan_name = k.kindergan_name AND t.kindergan_class = k.class 
									  INNER JOIN parents p ON k.parent_id = p.ID
									  WHERE k.parent_id = ? AND k.kindergan_name = ? AND k.class = ?");
		$stmt->bind_param("ssi", $parent_id, $kindergan_name, $kindergan_class);	
		$stmt->execute();
		$result = $stmt->get_result();
		while ($row = $result->fetch_assoc())
		{
			$teachers[] = $row;
		}
		$stmt->close();
		return $teachers;
	}
	
	/**
     * Get required Kids by kindergan name, kindergan class as parameters
     */
	public function GetRequiredKids($kindergan_name, $kindergan_class)
	{
		$stmt = $this->conn->prepare("SELECT * FROM kids WHERE kindergan_name = ? AND class = ?");
		$stmt->bind_param("si", $kindergan_name, $kindergan_class);
		$stmt->execute();
		$result = $stmt->get_result();
		while ($row = $result->fetch_assoc())
		{
			$kids[] = $row;
		}
		$stmt->close();
		return $kids;
	}
	
	/**
     * Get required Parents by kindergan name, kindergan class as parameters
     */
	public function GetRequiredParents($kindergan_name, $kindergan_class)
	{
		$stmt = $this->conn->prepare("SELECT p.ID, p.first_name, p.last_name, p.address, p.phone, p.email
									  FROM parents p, kids k 
									  WHERE p.ID = k.parent_id AND kindergan_name = ? AND class = ?");
		$stmt->bind_param("si", $kindergan_name, $kindergan_class);
		$stmt->execute();
		$result = $stmt->get_result();
		while ($row = $result->fetch_assoc())
		{
			$parents[] = $row;
		}
		$stmt->close();
		return $parents;
	}
	
	/**
     * Get required kids attendance by kindergan name, kindergan class as parameters
     */
	public function GetRequiredAttendance($kindergan_name, $kindergan_class)
	{
		$stmt = $this->conn->prepare("SELECT * FROM attendance WHERE kindergan_name = ? AND class = ?");
		$stmt->bind_param("si", $kindergan_name, $kindergan_class);
		$stmt->execute();
		$result = $stmt->get_result();
		while ($row = $result->fetch_assoc())
		{
			$attendance[] = $row;
		}
		$stmt->close();
		return $attendance;
	}
	
	/**
     * Get required presence of kid by parent id as parameter
     */
	public function GetRequiredPresence($parent_id)
	{
		$stmt = $this->conn->prepare("SELECT presence FROM kids WHERE parent_id = ?");
		$stmt->bind_param("s", $parent_id);
		if ($stmt->execute())
		{
			$presence = $stmt->get_result()->fetch_assoc();
			$stmt->close();
			
			return $presence;
		}
		else
		{
			return Null;
		}
	}
	
	/**
     * Update special column in kids table for specific kid
     */
	public function updateSpecial($parent_id, $special)
	{
		$stmt = $this->conn->prepare("UPDATE kids SET special = ?, updated_at = NOW() WHERE parent_id = ?");
		$stmt->bind_param("ss", $special, $parent_id);
		$result = $stmt->execute();
		$stmt->close();
		
		// check for successful update
		if ($result)
		{
			$stmt = $this->conn->prepare("SELECT * FROM kids WHERE parent_id = ? AND special = ?");
			$stmt->bind_param("ss", $parent_id, $special);	
			$stmt->execute();
			$kid = $stmt->get_result()->fetch_assoc();
			$stmt->close();
			
			return $kid;
		}
		else
		{
			return false;
		}
	}
	
	/**
     * Update contacts columns in kids table for specific kid
     */
	public function updateContactList($parent_id, $contact1, $contact2, $contact3)
	{
		$stmt = $this->conn->prepare("UPDATE kids SET contact1 = ?, contact2 = ?, contact3 = ?, updated_at = NOW() WHERE parent_id = ?");
		$stmt->bind_param("ssss", $contact1, $contact2, $contact3, $parent_id);
		$result = $stmt->execute();
		$stmt->close();
		
		// check for successful update
		if ($result)
		{
			$stmt = $this->conn->prepare("SELECT * FROM kids WHERE parent_id = ? AND contact1 = ? AND contact2 = ? AND contact3 = ?");
			$stmt->bind_param("ssss", $parent_id, $contact1, $contact2, $contact3);	
			$stmt->execute();
			$kid = $stmt->get_result()->fetch_assoc();
			$stmt->close();
			
			return $kid;
		}
		else
		{
			return false;
		}
	}

	/**
     * Update presence column in kids table for specific kid
     */
	public function updatePresence($parent_id, $presence)
	{
		$stmt = $this->conn->prepare("UPDATE kids SET presence = ?, updated_at = NOW() WHERE parent_id = ?");
		$stmt->bind_param("is", $presence, $parent_id);
		$result = $stmt->execute();
		$stmt->close();
		
		// check for successful update
		if ($result)
		{
			$stmt = $this->conn->prepare("SELECT * FROM kids WHERE parent_id = ? AND presence = ?");
			$stmt->bind_param("si", $parent_id, $presence);	
			$stmt->execute();
			$kid = $stmt->get_result()->fetch_assoc();
			$stmt->close();
			
			return $kid;
		}
		else
		{
			return false;
		}
	}
	
	/**
     * Update day column in attendance table for specific kid
     */
	public function updateAttendance($parent_id, $day, $presence)
	{
		switch ($day)
		{
			case '1':
			case '21':
			case '31':
			{
				$day .= "st";
				break;
			}
			case '2':
			case '22':
			{
				$day .= "nd";
				break;
			}
			case '3':
			case '23':
			{
				$day .= "rd";
				break;
			}
			default:
			{
				$day .= "th";
				break;
			}
		}
		
		$stmt = $this->conn->prepare("UPDATE attendance SET " . $day . " = ? WHERE parent_id = ?");
		$stmt->bind_param("is", $presence, $parent_id);
		$result = $stmt->execute();
		$stmt->close();
		
		// check for successful update
		if ($result)
		{
			$stmt = $this->conn->prepare("SELECT * FROM attendance WHERE parent_id = ? AND " . $day . " = ?");
			$stmt->bind_param("si", $parent_id, $presence);	
			$stmt->execute();
			$kid = $stmt->get_result()->fetch_assoc();
			$stmt->close();
			
			return $kid;
		}
		else
		{
			return false;
		}
	}
	
	/**
     * Update attendance table to default values in the days
     */
	public function resetAttendance()
	{
		$stmt = $this->conn->prepare("UPDATE attendance SET 1st = FALSE, 2nd = FALSE, 3rd = FALSE, 4th = FALSE, 5th = FALSE,
									  6th = FALSE, 7th = FALSE, 8th = FALSE, 9th = FALSE, 10th = FALSE, 11th = FALSE,
									  12th = FALSE, 13th = FALSE, 14th = FALSE, 15th = FALSE, 16th = FALSE, 17th = FALSE,
									  18th = FALSE, 19th = FALSE, 20th = FALSE, 21st = FALSE, 22nd = FALSE, 23rd = FALSE,
									  24th = FALSE, 25th = FALSE, 26th = FALSE, 27th = FALSE, 28th = FALSE, 29th = FALSE,
									  30th = FALSE, 31st = FALSE");
		$result = $stmt->execute();
		$stmt->close();
	}
	
	/**
     * Update presence in kids table to default value
     */
	public function resetPresence()
	{
		$stmt = $this->conn->prepare("UPDATE kids SET presence = 0");
		$result = $stmt->execute();
		$stmt->close();
	}
	
	/**
     * Update notification time column in teachers table for specific teachers
     */
	public function updateNotification($kindergan_name, $kindergan_class, $notification_time)
	{
		$stmt = $this->conn->prepare("UPDATE teachers SET notification_time = ?, updated_at = NOW() WHERE kindergan_name = ? AND kindergan_class = ?");
		$stmt->bind_param("ssi", $notification_time, $kindergan_name, $kindergan_class);
		$result = $stmt->execute();
		$stmt->close();
		
		// check for successful update
		if ($result)
		{
			$stmt = $this->conn->prepare("SELECT * FROM teachers WHERE kindergan_name = ? AND kindergan_class = ? AND notification_time = ?");
			$stmt->bind_param("sis", $kindergan_name, $kindergan_class, $notification_time);	
			$stmt->execute();
			$result = $stmt->get_result();
			while ($row = $result->fetch_assoc())
			{
				$teachers[] = $row;
			}
			$stmt->close();
			
			return $teachers;
		}
		else
		{
			return false;
		}
	}
	
	/**
     * Update schedule column in kindergans table for specific kindergarten
     */
	public function updateSchedule($kindergan_name, $kindergan_city, $kindergan_schedule)
	{
		$stmt = $this->conn->prepare("SELECT uid FROM kindergans WHERE name = ? AND city = ?");
		$stmt->bind_param("ss", $kindergan_name, $kindergan_city);	
		$stmt->execute();
		// Get the variable from the query.
		$stmt->bind_result($uid);
		// Fetch the data.
		$stmt->fetch();
		$stmt->close();
		
		// path of the image in the folder of schedule images with filename as the uid of the kindergan
		$path = "images/schedule/$uid.jpg";
		
		// Add the image file to server
		file_put_contents($path,base64_decode($kindergan_schedule));
		// Upload the image from server to external cloud
		$response = \Cloudinary\Uploader::upload("$path", array("public_id" => "images/schedule/$uid", "invalidate" => true));
		// Remove the image file from server
		unlink($path);
			
		$actualpath = "http://res.cloudinary.com/kindergan/image/upload/v$response[version]/$path";
		
		$stmt = $this->conn->prepare("UPDATE kindergans SET schedule_plan = ? WHERE name = ? AND city = ?");
		$stmt->bind_param("sss", $actualpath, $kindergan_name, $kindergan_city);
		$result = $stmt->execute();
		$stmt->close();
		
		// check for successful update
		if ($result)
		{
			$stmt = $this->conn->prepare("SELECT * FROM kindergans WHERE name = ? AND city = ? AND schedule_plan = ?");
			$stmt->bind_param("sss", $kindergan_name, $kindergan_city, $actualpath);	
			$stmt->execute();
			$gan = $stmt->get_result()->fetch_assoc();
			
			$stmt->close();
			return $gan;
		}
		else
		{
			return false;
		}
	}
	
	/**
     * Get schedule for specific kindergarten
     */
	public function getSchedule($kindergan_name)
	{
		// prepare and bind
		$stmt = $this->conn->prepare("SELECT * FROM kindergans WHERE name = ?");
		$stmt->bind_param("s", $kindergan_name);
		$stmt->execute();
		$gan = $stmt->get_result()->fetch_assoc();
		$stmt->close();
			
		return $gan;
	}
	
	/**
     * Encrypting password with default bcrypt algorithm
     * @param password
     * returns encrypted password+salt (salt is part of the hash)
     */
	public function hashBCRYPT($password)
	{
		$hash = password_hash($password, PASSWORD_DEFAULT);
		return $hash;
	}

	/**
     * Verifying password with hash value
     * @param password, hash
     * returns true if password verified, else return false
     */
	public function VerifyPassword($password, $hash)
	{
		if (password_verify($password, $hash)) 
		{
			// Success!
			return True;
		}
		else 
		{
			// Invalid credentials
			return False;
		}
	}
}

?>