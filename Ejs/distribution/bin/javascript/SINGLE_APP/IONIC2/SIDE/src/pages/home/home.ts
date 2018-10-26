import { ElementRef, Component, ViewChild } from '@angular/core';
import { Content, NavController, NavParams } from 'ionic-angular';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';

declare var app_toc: any;
declare var app_full_screen: any;

@Component({
  selector: 'page-home',
  templateUrl: 'home.html'
})
export class HomePage {
  @ViewChild(Content) content: Content;

  app_full_screen: boolean;
  hideNavigation: boolean;
  
  currentPage: any;
  pages: any;
  url: SafeUrl;

  constructor(public navCtrl: NavController, private sanitizer: DomSanitizer, public myElement: ElementRef, public navParams: NavParams) {
	// app vars
	this.app_full_screen = app_full_screen;
	this.pages = app_toc;
	
	// navigation vars
    this.currentPage = navParams.get("currentPage") || this.pages[0];
	this.url = sanitizer.bypassSecurityTrustResourceUrl(this.currentPage.url);
	
	// full screen
    this.hideNavigation = app_full_screen; 	// fullScreen requires hideNavigation
  }	

  controlScroll(event) {
	if (this.app_full_screen) {			
		let doc = (event.target.contentWindow || event.target.contentDocument);
		if (doc.document) doc = doc.document;
		let lastScrollTop = 0;
		let parent = this;
		doc.addEventListener("scroll", function(){
			let st = this.defaultView.pageYOffset;
			if (st > lastScrollTop){ // downscroll
				console.log("hide int");			
				let dimensions = parent.content.getContentDimensions();
				let bottom = dimensions.scrollTop + dimensions.scrollHeight;
				parent.content.scrollTo(0, bottom, 0);

				if(parent.hideNavigation == false) {
					let header = parent.myElement.nativeElement.getElementsByClassName('header')[0];
					header.hidden = true;
					parent.hideNavigation = true; 
					parent.content.resize();
				}
			}
			lastScrollTop = st;
		}, false);				
		
		// io-content scroll to bottom in order to hide navigation bar
		let dimensions = this.content.getContentDimensions();
		let bottom = dimensions.scrollTop + dimensions.scrollHeight;
		this.content.scrollTo(0, bottom, 0);
	}
  }
  
  ngAfterViewInit() {
	if (this.app_full_screen) {			
	    let header = this.myElement.nativeElement.getElementsByClassName('header')[0];
	    header.hidden = true;

		this.content.ionScroll.subscribe((event)=>{
		  let scrollamount = this.content.scrollTop;
      	  
		  // compare position scroll and maximum scroll
	      if (scrollamount == 0) { 
	        // show navigation bar when position scroll not is maximum
			console.log("show");
	        if(this.hideNavigation == true) {
			   this.hideNavigation = false;
			   let header = this.myElement.nativeElement.getElementsByClassName('header')[0];
			   header.hidden = false;
			   this.content.resize();
			}
	      } else {
	        // hide navigation bar when position scroll is maximum
			console.log("hide");
	        if(this.hideNavigation == false) {
			   this.hideNavigation = true; 
			   let header = this.myElement.nativeElement.getElementsByClassName('header')[0];
			   header.hidden = true;
			   this.content.resize();
			}
		  }
		});
	}  

  }

}
