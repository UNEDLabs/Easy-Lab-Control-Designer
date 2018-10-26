<?php
	// --------------------------------------
	// Write the error
	function PrintError($errcode){
	
		switch ($errcode){
			case 1: echo "Error: incorrect number of paramenters"; break;
			case 2: echo "Error: incorrect IP"; break;
			case 3: echo "Error: incorrect port"; break;
			case 4: echo "Error: incorrect user"; break;
			case 5: echo "Error: incorrect password"; break;
			case 6: echo "Error: incorrect package"; break;
			case 7: echo "Error: mainframe no valido"; break;
			case 8: echo "Error: incorrect dimension"; break;
			case 9: echo "Error: it can not read the file"; break;
			case 10: echo "Error: file blocked"; break;
			case 11: echo "Error: it can not create the HTML file"; break;
			case 12: echo "Error: HTML file blocked"; break;
			case 13: echo "Error: incorrect local/private IP"; break;
		   default:  echo "Error no defined"; break;
		}
		
	}
	
	//Check the IP address
	function IsIP($ip){
		$valid = FALSE;
		if (preg_match("/^[0-9]{1,3}(.[0-9]{1,3}){3}$/",$ip)){
			$valid = TRUE;
			foreach(explode(".", $ip) as $block){
				if( $block<0 || $block>255 )           
					$valid = FALSE;
			}
		}
		return $valid;
	}

	// Check the string
	function IsAlfa($str){
		$valid = FALSE;
		if (preg_match("/[^a-zA-Z0-9]/",$str)==0)
			$valid=TRUE;
		return $valid;
	}
	// --------------------------------------

	// If there are not parameters, it finish
	if (!isset($_POST['IP']) || !isset($_POST['PORT']) || !isset($_POST['USER']) || !isset($_POST['PASSWORD']) || !isset($_POST['PACKAGE']) || !isset($_POST['MAINFRAME']) || !isset($_POST['DIMENSION'])){
		PrintError(1);
		exit;
	}
	

	//IP filter
	$par_ip=trim($_POST['IP']);
	if ((strlen($par_ip)<7) || (strlen($par_ip)>15) || !IsIP($par_ip)){
		PrintError(2);
		exit;	
	}
	
	
	//Local-Public filter
	$par_iplp=trim($_POST['IPLP']);
	if ($par_iplp!='local' && $par_iplp!='public'){
		PrintError(13);
		exit;	
	}
	
	
	//Port filter
	$par_port=intval($_POST['PORT']);
	if (($par_port<1) || ($par_port>65535)){
		PrintError(3);
		exit;
	}
	
	
	//User filter
	$par_user=trim($_POST['USER']);
	if ((strlen($par_user)<3) || (strlen($par_user)>12) || !IsAlfa($par_user)){
		PrintError(4);
		exit;	
	}
	
	//Password filter
	$par_pass=trim($_POST['PASSWORD']);
	if ((strlen($par_pass)<2) || (strlen($par_pass)>12) || !IsAlfa($par_pass)){
		PrintError(5);
		exit;	
	}


	//Package filter
	$par_package=trim($_POST['PACKAGE']);
	if ((strlen($par_package)<3) || (strlen($par_package)>20)){
		PrintError(6);
		exit;	
	}
	
	
	//Mainframe filter
	$par_mainframe=trim($_POST['MAINFRAME']);
	if ((strlen($par_mainframe)<3) || (strlen($par_mainframe)>15) || !IsAlfa($par_mainframe)){
		PrintError(7);
		exit;	
	}
	
	
	//Dimension filter
	$par_dimension=trim($_POST['DIMENSION']);
	if ((strlen($par_dimension)<3) || (strlen($par_dimension)>15)){
		PrintError(8);
		exit;	
	}
	//String tokenizer
	$arrayDim = explode("-", $par_dimension);
	$width = intval($arrayDim[0]); // anchura
	$height = intval($arrayDim[1]); // altura
	
	
	//Read the template Html
	$html="";
	if ($fh=fopen("templateStudent.html",'r')){
		if(flock($fh,LOCK_SH)){
			$html=fread($fh,filesize("templateStudent.html"));
			flock($fh,LOCK_UN);
			fclose($fh);
		}
		else {
			PrintError(9);
			fclose($fh);
			exit;
		}
	}
	else {
		PrintError(10);
		exit;
	}
	
	//Formation of the strings
	$firstChar = substr($par_package,0,1);
	//$firstChar = strtolower($firstChar);
	$mainPackage = $firstChar.substr($par_package,1,strlen($par_package));
	$mainjar = substr($mainPackage,0,strlen($mainPackage)-4);
       //Cambios Carlos
      $archive = $mainjar.".jar";
	$codeapplet = $mainPackage.".".$mainjar."AppletStudent.class";

      //Creation of the string for the link
	$link = "javascript:document.".$mainjar."._simulation.startColMethod();document.".$mainjar."._simulation.update();";

	//Get the IP public
	if($_SERVER['HTTP_X_FORWARDED_FOR'])
	{
		if($pos=strpos($_SERVER['HTTP_X_FORWARDED_FOR']," "))
			$ipPublic = substr($_SERVER['HTTP_X_FORWARDED_FOR'],$pos+1);
		else
			$ipPublic = $_SERVER['HTTP_X_FORWARDED_FOR'];
	}
	else
		$ipPublic = $_SERVER['REMOTE_ADDR'];
		
		
	//VALUES REPLACEMENT****
	//Web page
	$html=str_replace("CODE_APPLET_VALUE",$codeapplet,$html);
	$html=str_replace("NAME_VALUE",$mainjar,$html);
	$html=str_replace("ARCHIVE_VALUE",$archive,$html);
	$html=str_replace("WIDTH_VALUE",$width,$html);
	$html=str_replace("HEIGHT_VALUE",$height,$html);
	$html=str_replace("LINK_VALUE",$link,$html);
	
	//Parameters
	if($par_iplp=='local')
	{
		$html=str_replace("IP_VALUE",$par_ip,$html);
		$ipFinal=$par_ip;
	}
	else
	{
		$html=str_replace("IP_VALUE",$ipPublic,$html);
		$ipFinal=$ipPublic;
	}
	
	$html=str_replace("PORT_VALUE",$par_port,$html);
	$html=str_replace("PACKAGE_VALUE",$par_package,$html);
	$html=str_replace("MAINFRAME_VALUE",$par_mainframe,$html);

	//Creation of the HtmlStudent
	$ipFinal=str_replace(".","",$ipFinal);

	$name = sprintf("student_%s.html", $par_user);
	if ($fh=fopen($name,'w')){
		if(flock($fh,LOCK_EX)){
			fwrite($fh,$html);
			fflush($fh);
			flock($fh,LOCK_UN);
			fclose($fh);
		}
		else {
			PrintError(11);
			fclose($fh);
			exit;
		}
	}
	else {
		PrintError(12);
		exit;
	}
	//Buffer return of the web page
	echo "http://".$_SERVER['HTTP_HOST'].dirname($_SERVER['PHP_SELF'])."/".$name;
	
?>

