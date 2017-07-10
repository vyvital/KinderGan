<?php
header('Content-Type: text/html; charset=utf-8');

if (isset($_POST['tag']) && $_POST['tag'] != '')
{
	// get tag
	$tag = $_POST['tag'];
	
	// include db handler
	require_once 'include/DBFunctions.php';
	$db = new DBFunctions();
	
	// response Array
	$response = array("tag" => $tag, "error" => FALSE);
	
	// check for tag type
	switch ($tag)
	{
		case 'login':
		// Request type is check Login
		{
			$email = $_POST['email'];
			$password = $_POST['password'];
			$type = $_POST['type'];
			$type_num = (int)$type;
			
			// check for user
			$user = $db->getUserByEmailAndPassword($email, $password, $type_num);
			if($user != false)
			// user found
			{
				if ($type_num == 1)
				// Nanny
				{
					$response["error"] = FALSE;
					$response["uid"] = $user["uid"];
					$response["user"]["ID"] = $user["ID"];
					$response["user"]["type"] = $user["type"];
					$response["user"]["firstname"] = $user["first_name"];
					$response["user"]["lastname"] = $user["last_name"];
					$response["user"]["phone"] = $user["phone"];
					$response["user"]["photo"] = $user["photo"];
					$response["user"]["kindergan_name"] = $user["kindergan_name"];
					$response["user"]["kindergan_city"] = $user["kindergan_city"];
					$response["user"]["kindergan_class"] = $user["kindergan_class"];
					$response["user"]["email"] = $user["email"];
					$response["user"]["notification_time"] = $user["notification_time"];
					$response["user"]["created_at"] = $user["created_at"];
					$response["user"]["updated_at"] = $user["updated_at"];
					echo json_encode($response);
				}
				else if ($type_num == 2)
				// Parent
				{
					$response["error"] = FALSE;
					$response["user"]["ID"] = $user["ID"];
					$response["user"]["type"] = $user["type"];
					$response["user"]["firstname"] = $user["first_name"];
					$response["user"]["lastname"] = $user["last_name"];
					$response["user"]["address"] = $user["address"];
					$response["user"]["phone"] = $user["phone"];
					$response["user"]["email"] = $user["email"];
					$response["user"]["created_at"] = $user["created_at"];
					$response["user"]["updated_at"] = $user["updated_at"];
					$response["user"]["kid_name"] = $user["name"];
					$response["user"]["kid_birthdate"] = $user["birth_date"];
					$response["user"]["kid_photo"] = $user["photo"];
					$response["user"]["kid_class"] = $user["class"];
					$response["user"]["kindergan_name"] = $user["kindergan_name"];
					
					$response["user"]["presence"] = $user["presence"];
					$response["user"]["special"] = $user["special"];
					$response["user"]["contact1"] = $user["contact1"];
					$response["user"]["contact2"] = $user["contact2"];
					$response["user"]["contact3"] = $user["contact3"];
					echo json_encode($response);
				}
			}
			else
			// user not found
			{
				$response["error"] = TRUE;
				$response["error_msg"] = "Incorrect email or password";
				echo json_encode($response);
			}
				break;
		}
		case 'register_teacher':
		// Request type is Register new user (teacher)
		{
			$id = $_POST['ID'];
			$fname = $_POST['First_Name'];
			$lname = $_POST['Last_Name'];
			$phone = $_POST['Phone'];
			$photo = $_POST['Picture'];
			$kindergan_name = $_POST['KinderGan_Name'];
			$kindergan_city = $_POST['KinderGan_City'];
			$kindergan_class = $_POST['Class'];
			$class = (int)$kindergan_class;
			$email = $_POST['Email'];
			$password = $_POST['Password'];
			
			// check if user is already existed with the same email
			if($db->isTeacherExisted($email))
			{
				// user is already existed - error response
				$response["error"] = TRUE;
				$response["error_msg"] = "User already exist";
				echo json_encode($response);
			}
			else
			{		
				// create a new user
				$user = $db->storeTeacher($id, $fname, $lname, $phone, $photo, 
										  $kindergan_name, $kindergan_city, $class,
										  $email, $password);
				if($user)
				{
					// user stored successfully
					$response["error"] = FALSE;
					$response["uid"] = $user["uid"];
					$response["user"]["ID"] = $user["ID"];
					$response["user"]["firstname"] = $user["first_name"];
					$response["user"]["lastname"] = $user["last_name"];
					$response["user"]["phone"] = $user["phone"];
					$response["user"]["photo"] = $user["photo"];
					$response["user"]["kindergan_name"] = $user["kindergan_name"];
					$response["user"]["kindergan_city"] = $user["kindergan_city"];
					$response["user"]["kindergan_class"] = $user["kindergan_class"];
					$response["user"]["email"] = $user["email"];
					$response["user"]["notification_time"] = $user["notification_time"];
					$response["user"]["created_at"] = $user["created_at"];
					$response["user"]["updated_at"] = $user["updated_at"];
					echo json_encode($response);
				}
				else
				{
					// user failed to store
					$response["error"] = TRUE;
					$response["error_msg"] = "Error occured in Registration";
					echo json_encode($response);
				}
			}
			break;
		}
		case 'register_parent':
		// Request type is Register new user (parent)
		{
			$id = $_POST['ID'];
			$fname = $_POST['First_Name'];
			$lname = $_POST['Last_Name'];
			$address = $_POST['Address'];
			$phone = $_POST['Phone'];
			
			$kid_name = $_POST['Kid_Name'];
			$kid_birthdate = $_POST['Kid_BirthDate'];
			$photo = $_POST['Kid_Picture'];
			$kindergan_name = $_POST['KinderGan_Name'];
			$kid_class = $_POST['Kid_Class'];
			$class = (int)$kid_class;
			$email = $_POST['Email'];
			$password = $_POST['Password'];
			
			// check if user is already existed with the same email
			if($db->isParentExisted($email))
			{
				// user is already existed - error response
				$response["error"] = TRUE;
				$response["error_msg"] = "User already exist";
				echo json_encode($response);
			}
			else
			{		
				// create a new user
				$user = $db->storeParent($id, $fname, $lname, $address, $phone,
										 $kid_name, $kid_birthdate, $photo, $kindergan_name, $class,
										 $email, $password);
				// create new attendance record
				$attendance_record = $db->storeAttendance($kindergan_name, $class, $id, $kid_name);
				if (($user) && ($attendance_record))
				{
					// user and attendance stored successfully
					$response["error"] = FALSE;
					$response["user"]["ID"] = $user["ID"];
					$response["user"]["firstname"] = $user["first_name"];
					$response["user"]["lastname"] = $user["last_name"];
					$response["user"]["address"] = $user["address"];
					$response["user"]["phone"] = $user["phone"];
					$response["user"]["email"] = $user["email"];
					$response["user"]["created_at"] = $user["created_at"];
					$response["user"]["updated_at"] = $user["updated_at"];
					$response["user"]["kid_name"] = $user["name"];
					$response["user"]["kid_birthdate"] = $user["birth_date"];
					$response["user"]["kid_photo"] = $user["photo"];
					$response["user"]["kid_class"] = $user["class"];
					$response["user"]["kindergan_name"] = $user["kindergan_name"];
					$response["user"]["presence"] = $user["presence"];
					echo json_encode($response);
				}
				else
				{
					// user or attendance failed to store
					$response["error"] = TRUE;
					$response["error_msg"] = "Error occured in Registration";
					echo json_encode($response);
				}
			}
			break;
		}
		case 'get_kindergans':
		{
			// Load all kindergans
			$gans = $db->GetAllKinderGans();
			if($gans)
			{
				// gans loaded successfully
				$response["error"] = FALSE;
				foreach ($gans as $row)
				{
					$response["Gans"][] = array('uid' => $row["uid"], 
												'name' => $row["name"],
												'classes' => $row["classes"], 
												'address' => $row["address"], 
												'city' => $row["city"], 
												'phone' => $row["phone"]);
				}
				echo json_encode($response, JSON_UNESCAPED_UNICODE);
			}
			else
			{
				// gans failed to load
				$response["error"] = TRUE;
				$response["error_msg"] = "Error occured in Loading Kindergans";
				echo json_encode($response);
			}
			break;
		}
		case 'get_teachers':
		{
			$parent_id = $_POST['ID'];
			$kindergan_name = $_POST['KinderGan_Name'];
			$kindergan_class = $_POST['KinderGan_Class'];
			$kclass = (int) $kindergan_class;
			
			// Load the required teachers
			$teachers = $db->GetRequiredTeachers($parent_id, $kindergan_name, $kclass);
			if($teachers)
			{
				// teachers loaded successfully
				$response["error"] = FALSE;
				foreach ($teachers as $row)
				{
					$response["Teachers"][] = array('ID' => $row["ID"], 
												'first_name' => $row["first_name"],
												'last_name' => $row["last_name"],
												'phone' => $row["phone"], 
												'kindergan_name' => $row["kindergan_name"], 
												'kindergan_class' => $row["kindergan_class"],  
												'email' => $row["email"],
												'notification_time' => $row["notification_time"]);
				}
				echo json_encode($response, JSON_UNESCAPED_UNICODE);
			}
			else
			{
				// teachers failed to load
				$response["error"] = TRUE;
				$response["error_msg"] = "Error occured in Loading Teachers";
				echo json_encode($response);
			}
			break;
		}
		case 'get_kids':
		{
			$kindergan_name = $_POST['KinderGan_Name'];
			$kindergan_class = $_POST['KinderGan_Class'];
			$kclass = (int) $kindergan_class;
			
			// Load required kids
			$kids = $db->GetRequiredKids($kindergan_name, $kclass);
			// Load required parents
			$parents = $db->GetRequiredParents($kindergan_name, $kclass);
			if (($kids) && ($parents))
			{
				// kids and parents loaded successfully
				$response["error"] = FALSE;
				foreach ($kids as $row)
				{
					$response["Kids"][] = array('name' => $row["name"],
												'birth_date' => $row["birth_date"],
												'photo' => $row["photo"], 
												'kindergan_name' => $row["kindergan_name"], 
												'class' => $row["class"],  
												'parent_id' => $row["parent_id"],
												'presence' => $row["presence"],
												'special' => $row["special"],
												'contact1' => $row["contact1"],
												'contact2' => $row["contact2"],
												'contact3' => $row["contact3"]);
				}
				foreach ($parents as $row)
				{
					$response["Parents"][] = array('ID' => $row["ID"],
												'first_name' => $row["first_name"],
												'last_name' => $row["last_name"], 
												'address' => $row["address"], 
												'phone' => $row["phone"],  
												'email' => $row["email"]);
				}
				echo json_encode($response, JSON_UNESCAPED_UNICODE);
			}
			else
			{
				// kids and parents failed to load
				$response["error"] = TRUE;
				$response["error_msg"] = "Error occured in Loading Kids and Parents";
				echo json_encode($response);
			}
			break;
		}
		case 'get_attendance':
		{
			$kindergan_name = $_POST['KinderGan_Name'];
			$kindergan_class = $_POST['KinderGan_Class'];
			$kclass = (int) $kindergan_class;
			
			// Load the required kids attendance
			$attendance = $db->GetRequiredAttendance($kindergan_name, $kclass);
			if($attendance)
			{
				// attendance loaded successfully
				$response["error"] = FALSE;
				foreach ($attendance as $row)
				{
					$response["attendance"][] = array('uid' => $row["uid"],
												    'parent_id' => $row["parent_id"],
													'name' => $row["name"],
													'1st' => $row["1st"],
													'2nd' => $row["2nd"],
													'3rd' => $row["3rd"],
													'4th' => $row["4th"],  
													'5th' => $row["5th"],
													'6th' => $row["6th"],
													'7th' => $row["7th"],
													'8th' => $row["8th"],
													'9th' => $row["9th"],
													'10th' => $row["10th"],
													'11th' => $row["11th"],
													'12th' => $row["12th"],
													'13th' => $row["13th"],
													'14th' => $row["14th"],
													'15th' => $row["15th"],
													'16th' => $row["16th"],
													'17th' => $row["17th"],
													'18th' => $row["18th"],
													'19th' => $row["19th"],
													'20th' => $row["20th"],
													'21st' => $row["21st"],
													'22nd' => $row["22nd"],
													'23rd' => $row["23rd"],
													'24th' => $row["24th"],
													'25th' => $row["25th"],
													'26th' => $row["26th"],
													'27th' => $row["27th"],
													'28th' => $row["28th"],
													'29th' => $row["29th"],
													'30th' => $row["30th"],
													'31st' => $row["31st"]);
				}
				echo json_encode($response, JSON_UNESCAPED_UNICODE);
			}
			else
			{
				// attendance failed to load
				$response["error"] = TRUE;
				$response["error_msg"] = "Error occured in Loading kids attendance";
				echo json_encode($response);
			}
			break;
		}
		case 'get_schedule':
		{
			$kindergan_name = $_POST['KinderGan_Name'];
			
			// Load schedule to specific kindergarten
			$gan = $db->getSchedule($kindergan_name);
			if ($gan)
			{
				// gan loaded successfully
				$response["error"] = FALSE;
				$response["gan"]["uid"] = $gan["uid"];
				$response["gan"]["name"] = $gan["name"];
				$response["gan"]["classes"] = $gan["classes"];
				$response["gan"]["address"] = $gan["address"];
				$response["gan"]["city"] = $gan["city"];
				$response["gan"]["phone"] = $gan["phone"];
				$response["gan"]["schedule_plan"] = $gan["schedule_plan"];
				echo json_encode($response);
			}
			else
			{
				// gan failed to load
				$response["error"] = TRUE;
				$response["error_msg"] = "Error occured in Loading kindergan";
				echo json_encode($response);
			}
			break;
		}
		case 'get_presence':
		{
			$parent_id = $_POST['Parent_ID'];
			
			// Load presence of specific kid
			$presence = $db->GetRequiredPresence($parent_id);
			if ($presence != null)
			{
				// pesence loaded successfully
				$response["error"] = FALSE;
				$response["kid"]["presence"] = $presence["presence"];
				echo json_encode($response);
			}
			else
			{
				// presence failed to load
				$response["error"] = TRUE;
				$response["error_msg"] = "Error occured in Loading presence";
				echo json_encode($response);
			}
			break;
		}
		case 'update_special_request':
		{
			$parent_id = $_POST['parent_id'];
			$special = $_POST['special'];
			
			// Update special request to specific kid in kids table
			$kid = $db->updateSpecial($parent_id, $special);
			if($kid)
			{
				// kid updated successfully
				$response["error"] = FALSE;
				$response["kid"]["special"] = $kid["special"];
				$response["kid"]["updated_at"] = $kid["updated_at"];
				echo json_encode($response);
			}
			else
			{
				// kid failed to update
				$response["error"] = TRUE;
				$response["error_msg"] = "Error occured in Update";
				echo json_encode($response);
			}
			break;
		}
		case 'update_pickup_list':
		{
			$parent_id = $_POST['parent_id'];
			$contact1 = $_POST['contact1'];
			$contact2 = $_POST['contact2'];
			$contact3 = $_POST['contact3'];
			
			// Update contact list to specific kid in kids table
			$kid = $db->updateContactList($parent_id, $contact1, $contact2, $contact3);
			if($kid)
			{
				// kid updated successfully
				$response["error"] = FALSE;
				$response["kid"]["contact1"] = $kid["contact1"];
				$response["kid"]["contact2"] = $kid["contact2"];
				$response["kid"]["contact3"] = $kid["contact3"];
				$response["kid"]["updated_at"] = $kid["updated_at"];
				echo json_encode($response);
			}
			else
			{
				// kid failed to update
				$response["error"] = TRUE;
				$response["error_msg"] = "Error occured in Update";
				echo json_encode($response);
			}
			break;
		}
		case 'update_presence':
		{
			$parent_id = $_POST['parent_id'];
			$day = $_POST['day'];
			$presence = $_POST['presence'];
			$kid_presence = (int) $presence;
			
			// Update presence to specific kid in kids table
			$kid = $db->updatePresence($parent_id, $kid_presence);
			// Update presence to specific kid in attendance table
			$kid_attendance = $db->updateAttendance($parent_id, $day, $kid_presence);
			if (($kid) && ($kid_attendance))
			{
				// kid updated successfully
				$response["error"] = FALSE;
				$response["kid"]["presence"] = $kid["presence"];
				$response["kid"]["updated_at"] = $kid["updated_at"];
				echo json_encode($response);
			}
			else
			{
				// kid failed to update
				$response["error"] = TRUE;
				$response["error_msg"] = "Error occured in Update";
				echo json_encode($response);
			}
			break;
		}
		case 'update_notification':
		{
			$kindergan_name = $_POST['KinderGan_Name'];
			$kindergan_class = $_POST['KinderGan_Class'];
			$kclass = (int) $kindergan_class;
			$notification_time = $_POST['Notification_Time'];
			
			// Update notification time to all teachers in specific class in teachers table
			$teachers = $db->updateNotification($kindergan_name, $kclass, $notification_time);
			if($teachers)
			{
				// teachers updated successfully
				$response["error"] = FALSE;
				foreach ($teachers as $row)
				{
					$response["Teachers"][] = array('ID' => $row["ID"], 
												'first_name' => $row["first_name"],
												'last_name' => $row["last_name"],
												'phone' => $row["phone"], 
												'kindergan_name' => $row["kindergan_name"], 
												'kindergan_class' => $row["kindergan_class"],  
												'email' => $row["email"],
												'notification_time' => $row["notification_time"],
												'updated_at' => $row["updated_at"]);
				}
				echo json_encode($response, JSON_UNESCAPED_UNICODE);
			}
			else
			{
				// teachers failed to update
				$response["error"] = TRUE;
				$response["error_msg"] = "Error occured in Update";
				echo json_encode($response);
			}
			break;
		}
		case 'upload_schedule':
		{
			$kindergan_name = $_POST['KinderGan_Name'];
			$kindergan_city = $_POST['KinderGan_City'];
			$kindergan_schedule = $_POST['Schedule'];
			
			// Update schedule to specific kindergarten in kindergans table
			$gan = $db->updateSchedule($kindergan_name, $kindergan_city, $kindergan_schedule);
			if ($gan)
			{
				// gan updated successfully
				$response["error"] = FALSE;
				$response["gan"]["schedule_plan"] = $gan["schedule_plan"];
				echo json_encode($response);
			}
			else
			{
				// gan failed to update
				$response["error"] = TRUE;
				$response["error_msg"] = "Error occured in Update";
				echo json_encode($response);
			}
			break;
		}
		default:
		// Request type is unknown
		{
			$response["error"] = TRUE;
			$response["error_msg"] = "Unknown 'tag' value";
			echo json_encode($response);
		}
	}
}
else
{
	// Display poster image
	$path = "images/poster.jpg";
	echo "<img src='$path' alt='starting' style='width:100%;'>";
}

?>