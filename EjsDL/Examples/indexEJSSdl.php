<?php

  function endsWith($FullStr, $EndStr) {
    $StrLen = strlen($EndStr); // Get the length of the end string
    $FullStrEnd = substr($FullStr, strlen($FullStr) - $StrLen); // Look at the end of FullStr for the substring the size of EndStr
    return $FullStrEnd == $EndStr; // If it matches, it does end with EndStr
  }
  
  function startsWith($haystack, $needle) {
    return substr($haystack, 0, strlen($needle)) === $needle;
  }

  function listDir($prefix,$baseAddress, $dir, $tab) { 
	$tab = $tab . "  ";

	#Get the directory listing and sort it
    $dh = opendir($dir);
	while (false !== ($filename = readdir($dh))) $files[] = $filename;
	sort($files);
	
	# process recursively the subdirectories first
	foreach($files as $filename) {
      if ($filename=="." || $filename=="..") continue;
      if ($filename[0]=="_") continue; // Ignore directories starting with '_'
	  
	  $path = $dir . "/" . $filename;
      if (is_dir($path)) {
	    # entry for the directory
	    print $tab . '<dir url="' . $baseAddress . $filename . '/"';
	    if (file_exists($path . "/info.html")) print ' html="info.html"';
	    print ' name="' . $filename . '">' . "\n";
	    listDir ($prefix,$baseAddress . $filename . "/", $path, $tab);
	    print $tab . "</dir>\n";
	  }
    }
	# process now the models (ZIP files)  
	$prefixLength = strlen($prefix);
	foreach($files as $filename) {
      if (is_dir($dir . "/" . $filename) ) continue; // Only non directories
      if ($filename=="." || $filename=="..") continue;
      if ($filename[0]=="_") continue; // Ignore files starting with '_'
      if (!startsWith($filename,$prefix)) continue; // Only files with the correct prefix
	  if (!endsWith($filename,".zip")) continue; // only zip files
      
	  $plainname = substr($filename,0,strlen($filename)-4);
	  $name = substr($plainname,$prefixLength);
	  print $tab . "<model";
	  print ' name="' . $name . '"';
      $htmlfile = $plainname . ".html";
	  if (file_exists($dir . "/" . $htmlfile)) print ' html="' . $htmlfile . '"';
	  else {
	    $htmlfile = "ejss_html_" . $name . ".html";
	    if (file_exists($dir . "/" . $htmlfile)) print ' html="' . $htmlfile . '"';
	  }
	  print ' zip="' . $filename . '">' . $plainname . "</model>\n";
    }
	
  }

  $baseAddress =  "http://" . $_SERVER['SERVER_NAME'] . $_SERVER['REQUEST_URI'];
  $baseAddress = substr($baseAddress,0,strlen($baseAddress)-strlen(strrchr($baseAddress, "/"))+1);
  $prefix = $_GET["prefix"];
  if ($prefix=="") $prefix = "ejss_model_";
  
  print '<?xml version="1.0" encoding="UTF-8"?>' . "\n";
  print '<dir url="' . $baseAddress . '"'; 
  if (file_exists("info.html")) print ' html="info.html"';
  print ">\n";

  listDir ($prefix,$baseAddress,".","  ");
  print "</dir>\n";
	
?>
