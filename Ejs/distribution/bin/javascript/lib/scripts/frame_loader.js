function frame_loader(port) {
  var ws;
  try {
    ws = new WebSocket("ws://localhost:"+port);
    console.log('Connecting... (readyState ' + ws.readyState + ')');
    ws.onerror = function(evt) {
      console.log("Error Event: " + evt.type + " - Message: " + evt.data);
    }
    ws.onmessage = function(evt) {
      processInput(evt.data);
    }
    ws.onclose = function(evt) {
      closeWindows();
    }
  }
  catch(exception) {
    console.log(exception);
  }

  function processInput(input) {
    console.log ("Processing input: "+input);
    var iframe = document.getElementById('webview');
    switch (input.charAt(0)) {
      case 'Q' : ws.close(); closeWindows(); break; // close window;
      case 'S' : 
        var res = input.substring(1).split(" ");
        console.log ("Setting size to "+res[0]+","+res[1]);
        iframe.width = res[0];
        iframe.height = res[1];
        //iframe.attr("height", res[1]);
        break;
      case 'R' : 
        iframe.src = input.substring(1);
        break;
    }
  }; 
  
  function closeWindows() {
    var browserName = navigator.appName;
    var browserVer = parseInt(navigator.appVersion);
    //alert(browserName + " : "+browserVer);

    //document.getElementById("flashContent").innerHTML = "<br>&nbsp;<font face='Arial' color='blue' size='2'><b> You have been logged out of the Game. Please Close Your Browser Window.</b></font>";

    if(browserName == "Microsoft Internet Explorer"){
       var ie7 = (document.all && !window.opera && window.XMLHttpRequest) ? true : false;  
       if (ie7) {  
         //This method is required to close a window without any prompt for IE7 & greater versions.
         window.open('','_parent','');
         window.close();
       }
       else {
         //This method is required to close a window without any prompt for IE6
         this.focus();
         self.opener = this;
         self.close();
       }
     }
     else {  
       //For NON-IE Browsers except Firefox which doesnt support Auto Close
       try {
         this.focus();
         self.opener = this;
         self.close();
       } catch(e) { }
       try {
         window.open('','_self','');
         window.close();
       } catch(e) { }
     }
  };
  
};
  