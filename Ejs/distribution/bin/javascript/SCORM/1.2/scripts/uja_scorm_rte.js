/* RTE JavaScript library by I. Ruano (alonso@ujaen.es) is licensed under a Creative Commons Attribution 4.0 International License.
Librería desarrollada por I. Ruano Ruano. Se permite cualquier explotación de la obra, incluyendo una finalidad comercial, así como la creación de obras derivadas, la distribución de las cuales también está permitida sin ninguna restricción.
En cualquier explotación de la obra autorizada por la licencia hará falta reconocer la autoría.
*/
var _scorm;
var _scorm_terminated;
function _scorm_loadPage(ver) {
	_scorm = new RTE(ver);
	_scorm_terminated = false;
  var result = _scorm.initialize();
  _scorm.setCompletionStatus("incomplete");
}
function _scorm_unloadPage() {
  if (_scorm_terminated) return;
  _scorm.setExit("");
  _scorm.setSessionTime();
  _scorm.terminate();
  _scorm_terminated = true;
}
function timeStamp(ver) {
		var d = new Date();
		var hora = d.getHours();
		var minuto = d.getMinutes();
		var segundo = d.getSeconds();
		var dia = d.getDate();
		var mes = d.getMonth() + 1;
		if (mes < 10) {mes = "0" + mes;}
		if (dia < 10) {dia = "0" + dia;}
		if (hora < 10) {hora = "0" + hora;}
		if (minuto < 10) {minuto = "0" + minuto;}
		if (segundo < 10) {segundo = "0" + segundo;}
		var response="";
		return response=(ver=="2004")?d.getFullYear() + "-" + mes + "-" + dia + "T" + hora + ":" + minuto + ":" + segundo:hora + ":" + minuto + ":" + segundo;////SCORM 2004 or 1.2
	}
var RTE = function (ver) {
	var startDate = new Date();
	this.startMS = startDate.getTime();
	this.verScorm=ver; //SCORM VERSION: 2004 or 1.2
/* ***************************** Read Only elements (constant in session)*/
    this.learnerName; //Learner name in the LMS
    this.learnerId; //Learner identification in the LMS
	this.completionThreshold; 
	this.credit;
	this.entry; //contains information that asserts whether the learner has previously accessed the SCO: "ab-initio", "resume", ""
	this.launchData;
	this.maxTimeAllowed;
	this.timeLimitAction;
	this.mode;
	this.totalTime;
	this.scoreChildren;	
	this.interactionsChildren;	
	this.learnerPreferenceChildren;
	this.objectivesChildren;
	this.startTimeStamp = timeStamp(this.verScorm);
	if (this.verScorm=="2004"){ //SCORM2004v4 exclusive data
		this.version; //Data model version supported by the LMS
		this.scaledPassingScore;
		this.commentsLearnerChildren;
		this.commentsLMSChildren;
		this.dataChildren;
	} else{	//SCORM1.2 exclusive data
		this.studentDataChildren; //It indicates student data elements supported by the LMS: mastery_score, max_time_allowed, time_limit_action
		this.coreChildren;
	}
/* ***************************** Read Only variables (not constant in session)*/
	this.interactionsCount;
	this.objectivesCount;
	if (this.verScorm=="2004"){ //SCORM2004v4 exclusive data
		this.commentsLearnerCount;
		this.commentsLMSCount;
		this.dataCount;
		this.navReqValidContinue;
		this.navReqValidPrevious;
/*	this.navReqValidChoice = doGetValue("adl.nav.request_valid.choice.{target=<string>}");
	this.navReqValidJump = doGetValue("adl.nav.request_valid.jump.{target=<string>}");	*/
	}
/* ***************************** W/R variables */
	this.rteLocation;
	this.suspendData; //provides additional space to store and retrieve suspend data between learner sessions; suspend data may be used by the SCO to resume the learner attempt
	this.completionStatus;	//It indicates whether the learner has completed the SCO. Restricted values:
		// SCO1.2->passed, completed, failed, incomplete, browsed or not attempted
		// SCO2004->completed, incomplete, not attempted, unknown.
	this.scoreRaw;
	this.scoreMin;
	this.scoreMax;
	this.learnerPreferenceAudio;
	this.learnerPreferenceLanguage;
	this.learnerPreferenceSpeed;
	this.learnerPreferenceCaptioning;
	if (this.verScorm=="2004"){ //SCORM2004v4 exclusive data
		this.progressMeasure; //is a measure of the progress the learner has made toward completing the SCO (completionStatus-if completionThreshold no defined-=>0:not attempted,0<pm<1:incomplete,1:completed)
		this.successStatus; //It indicates whether the learner has mastered the SCO. Restricted values: passed, failed, unknown
		this.scoreScaled; //It reflects the performance of the learner (-1.0 to 1.0)
		this.navRequest; //indicate a desired navigation request to be processed immediately following the SCO successfully invoking Terminate()
	}
/* ***************************** Write only variables */
	this.exit; //Indicates how or why the learner left the SCO:
		//SCORM1.2->suspend, logout, time-out, ""
		//SCORM2004->suspend, normal, logout, time-out, ""
	this.sessionTime; // Identifies the amount of time that the learner has spent in the current learner session for the SCO
/* ***************************** W/R arrays */
	this.interactionsId = new Array();
	this.interactionsInitTime = new Array();
	this.interactionsType = new Array();
	this.interactionsObjectivesCount = new Array();
	this.interactionsObjectivesId = new Array();
	this.interactionsTs = new Array();
	this.interactionsCorrectRespCount = new Array();
	this.interactionsCorrectRespPattern = new Array();
	this.interactionsWeight = new Array();
	this.interactionsResponse = new Array();
	this.interactionsResult = new Array();
	this.interactionsLatency = new Array();
	this.objectivesId = new Array();
	this.objectivesScoreRaw = new Array();
	this.objectivesScoreMin = new Array();
	this.objectivesScoreMax = new Array();
	this.objectivesCompletionStatus = new Array();
	if (this.verScorm=="2004"){ //SCORM2004v4 exclusive data
		this.commentsLearner = new Array();
		this.commentsLearnerLoc = new Array();
		this.commentsLearnerTs = new Array();
		this.commentsLMS = new Array();
		this.commentsLMSLoc = new Array();
		this.commentsLMSTs = new Array();
		this.interactionsDescription = new Array();
		this.objectivesSuccessStatus = new Array();
		this.objectivesScoreScaled = new Array();
		this.objectivesProgressMeasure = new Array();
		this.objectivesDescription = new Array();
		this.dataId = new Array();
		this.dataStore = new Array();
	} else{
		this.commentsLearner;
		this.commentsLMS;
	}
};
/* ***************************** Connection functions */
RTE.prototype.initialize = function(){
	var response;
	response=(this.verScorm=="2004")?doInitialize():doLMSInitialize();
	return response;
};
RTE.prototype.terminate = function(){
	var response;
	response=(this.verScorm=="2004")?doTerminate():doLMSFinish();
	return response;
};
/* ***************************** Persistent data store functions */
RTE.prototype.commit = function(){
	var response;
	response=(this.verScorm=="2004")?doCommit():doLMSCommit();
	return response;
};
/* ***************************** Error manage functions */
RTE.prototype.getLastError = function(){
	var response;
	response=(this.verScorm=="2004")?doGetLastError():doLMSGetLastError();
	return response;
};
RTE.prototype.getErrorString = function(errorCode){
	var response;
	response=(this.verScorm=="2004")?doGetErrorString(errorCode):doLMSGetErrorString(errorCode);
	return response;
};
RTE.prototype.getDiagnostic = function(errorCode){
	var response;
	response=(this.verScorm=="2004")?doGetDiagnostic(errorCode):doLMSGetDiagnostic(errorCode);
	return response;
};
/* ***************************** Auxiliar functions */
RTE.prototype.getWorkingSeconds = function(msec){  // Calculate seconds elapsed from given timestamp in msec
	var currentDate= new Date();
	var currentTime=currentDate.getTime();
	currentTime = (currentTime - msec)/1000;
	return currentTime;
};
RTE.prototype.getLatencyFromSeconds = function(secs){  // Convert seconds to SCORM timeinterval format (latency)
	var hour=secs/3600;
	hour=Math.trunc(hour)//hour.toFixed();
	var min=(secs-hour*3600)/60;
	min=Math.trunc(min)//min.toFixed();
	var sec=secs-hour*3600-min*60;
	sec=sec.toFixed(2);
	if (hour < 10) hour = "0" + hour;
	if (min < 10) min = "0" + min;
	if (sec < 10) sec = "0" + sec;
	var response=(this.verScorm=="2004")?"PT"+hour+"H"+min+"M"+sec+"S":hour+":"+min+":"+sec;
	return response;
};
/* ***************************** Generic data transfer functions */
RTE.prototype.getData = function(data){
	var response;
	response=(this.verScorm=="2004")?doGetValue(data):doLMSGetValue(data);
	return response;
};
RTE.prototype.setData = function(data, value){
	var response;
	response=(this.verScorm=="2004")?doSetValue(data, value):doLMSSetValue(data, value);
	return response;
};
/* ***************************** Read Only variables (constant in session)*/
RTE.prototype.getLearnerName = function(){
	if(typeof this.learnerName === "undefined") this.learnerName =(this.verScorm=="2004")?doGetValue("cmi.learner_name"):doLMSGetValue("cmi.core.student_name");
	return this.learnerName;
};
RTE.prototype.getLearnerId = function(){
	if(typeof this.learnerId === "undefined") this.learnerId =(this.verScorm=="2004")?doGetValue("cmi.learner_id"):doLMSGetValue("cmi.core.student_id");
	return this.learnerId;
};
RTE.prototype.getCompletionThreshold = function(){
	if(typeof this.completionThreshold === "undefined") this.completionThreshold =(this.verScorm=="2004")?doGetValue("cmi.completion_threshold"):doLMSGetValue("cmi.student_data.mastery_score");
	return this.completionThreshold;
};
RTE.prototype.getCredit = function(){
	if(typeof this.credit === "undefined") this.credit =(this.verScorm=="2004")?doGetValue("cmi.credit"):doLMSGetValue("cmi.core.credit");
	return this.credit;
};
RTE.prototype.getEntry = function(){
	if(typeof this.entry === "undefined") this.entry =(this.verScorm=="2004")?doGetValue("cmi.entry"):doLMSGetValue("cmi.core.entry");
	return this.entry;
};
RTE.prototype.getLaunchData = function(){
	if(typeof this.launchData === "undefined") this.launchData =(this.verScorm=="2004")?doGetValue("cmi.launch_data"):doLMSGetValue("cmi.launch_data");
	return this.launchData;
};
RTE.prototype.getMaxTimeAllowed = function(){
	if(typeof this.maxTimeAllowed === "undefined") this.maxTimeAllowed =(this.verScorm=="2004")?doGetValue("cmi.max_time_allowed"):doLMSGetValue("cmi.student_data.max_time_allowed");
	return this.maxTimeAllowed;
};
RTE.prototype.getTimeLimitAction = function(){
	if(typeof this.timeLimitAction === "undefined") this.timeLimitAction =(this.verScorm=="2004")?doGetValue("cmi.time_limit_action"):doLMSGetValue("cmi.student_data.time_limit_action");
	return this.timeLimitAction;
};
RTE.prototype.getMode = function(){
	if(typeof this.mode === "undefined") this.entry =(this.verScorm=="2004")?doGetValue("cmi.mode"):doLMSGetValue("cmi.core.lesson_mode");
	return this.mode;
};
RTE.prototype.getTotalTime = function(){
	if(typeof this.totalTime === "undefined") this.totalTime =(this.verScorm=="2004")?doGetValue("cmi.total_time"):doLMSGetValue("cmi.core.total_time");
	return this.totalTime;
};
RTE.prototype.getScoreChildren = function(){
	if(typeof this.scoreChildren === "undefined") this.scoreChildren =(this.verScorm=="2004")?doGetValue("cmi.score._children"):doLMSGetValue("cmi.core.score._children");
	return this.scoreChildren;
};
RTE.prototype.getInteractionsChildren = function(){
	if(typeof this.interactionsChildren === "undefined") this.interactionsChildren =(this.verScorm=="2004")?doGetValue("cmi.interactions._children"):doLMSGetValue("cmi.interactions._children");
	return this.interactionsChildren;
};
RTE.prototype.getLearnerPreferenceChildren = function(){
	if(typeof this.learnerPreferenceChildren === "undefined") this.learnerPreferenceChildren =(this.verScorm=="2004")?doGetValue("cmi.learner_preference._children"):doLMSGetValue("cmi.student_preference._children");
	return this.learnerPreferenceChildren;
};
RTE.prototype.getObjectivesChildren = function(){
	if(typeof this.objectivesChildren === "undefined") this.objectivesChildren =(this.verScorm=="2004")?doGetValue("cmi.objectives._children"):doLMSGetValue("cmi.objectives._children");
	return this.objectivesChildren;
};
RTE.prototype.getVersion = function(){ // SCORM2004v4 exclusive function
	if(typeof this.version === "undefined") this.version = doGetValue("cmi._version");
	return this.version;
};
RTE.prototype.getScaledPassingScore = function(){ // SCORM2004v4 exclusive function
	if(typeof this.scaledPassingScore === "undefined") this.scaledPassingScore = doGetValue("cmi.scaled_passing_score");
	return this.scaledPassingScore;
};
RTE.prototype.getCommentsLearnerChildren = function(){ // SCORM2004v4 exclusive function
	if(typeof this.commentsLearnerChildren === "undefined") this.commentsLearnerChildren = doGetValue("cmi.comments_from_learner._children");
	return this.commentsLearnerChildren;
};
RTE.prototype.getCommentsLMSChildren = function(){ // SCORM2004v4 exclusive function
	if(typeof this.commentsLMSChildren === "undefined") this.commentsLMSChildren = doGetValue("cmi.comments_from_lms._children");
	return this.commentsLMSChildren;
};
RTE.prototype.getDataChildren = function(){ // SCORM2004v4 exclusive function
	if(typeof this.dataChildren === "undefined") this.dataChildren = doGetValue("adl.data._children");
	return this.dataChildren;
};
RTE.prototype.getStudentDataChildren = function(){ // SCORM1.2 exclusive function
	if(typeof this.studentDataChildren === "undefined") this.studentDataChildren = doLMSGetValue("cmi.student_data._children");
	return this.studentDataChildren;
};
RTE.prototype.getCoreChildren = function(){ // SCORM1.2 exclusive function
	if(typeof this.coreChildren === "undefined") this.coreChildren = doLMSGetValue("cmi.core._children");
	return this.coreChildren;
};
/* ***************************** Read Only variables (not constant in session)*/
RTE.prototype.getInteractionsCount = function(){
	this.interactionsCount =(this.verScorm=="2004")?doGetValue("cmi.interactions._count"):doLMSGetValue("cmi.interactions._count");
	return this.interactionsCount;
};
RTE.prototype.getObjectivesCount = function(){
	this.objectivesCount =(this.verScorm=="2004")?doGetValue("cmi.objectives._count"):doLMSGetValue("cmi.objectives._count");
	return this.objectivesCount;
}
RTE.prototype.getCommentsLearnerCount = function(){ // SCORM2004v4 exclusive function
	this.commentsLearnerCount = doGetValue("cmi.comments_from_learner._count");
	return this.commentsLearnerCount;
};
RTE.prototype.getCommentsLMSCount = function(){ // SCORM2004v4 exclusive function
	this.commentsLMSCount = doGetValue("cmi.comments_from_lms._count");
	return this.commentsLMSCount;
};
RTE.prototype.getDataCount = function(){ // SCORM2004v4 exclusive function
	this.dataCount = doGetValue("adl.data._count");
	return this.dataCount;
};
RTE.prototype.getNavReqValidContinue = function(){ // SCORM2004v4 exclusive function
	this.navReqValidContinue = doGetValue("adl.nav.request_valid.continue");
	return this.navReqValidContinue;
};
RTE.prototype.getNavReqValidPrevious = function(){ // SCORM2004v4 exclusive function
	this.navReqValidPrevious = doGetValue("adl.nav.request_valid.previous");
	return this.navReqValidPrevious;
};
RTE.prototype.getNavReqValidChoice = function(target){ // SCORM2004v4 exclusive function
	var com="adl.nav.request_valid.choice.{target=" + target + "}";
	this.navReqValidChoice = doGetValue(com);
	return this.navReqValidChoice;
};
RTE.prototype.getNavReqValidJump = function(target){ // SCORM2004v4 exclusive function
	var com="adl.nav.request_valid.jump.{target=" + target + "}";
	this.navReqValidJump = doGetValue(com);
	return this.navReqValidJump;
};
/* ***************************** Read&Store R/W variables*/
RTE.prototype.getRTELocation = function(){
	if(typeof this.rteLocation === "undefined") this.rtelocation =(this.verScorm=="2004")?doGetValue("cmi.location"):doLMSGetValue("cmi.core.lesson_location");
	return this.rteLocation;
};
RTE.prototype.setRTELocation = function(loc){
	var ret =(this.verScorm=="2004")?doSetValue("cmi.location", loc):doLMSSetValue("cmi.core.lesson_location", loc);
	if (ret) this.rteLocation=loc;
	return ret;
};
RTE.prototype.getSuspendData = function(){
	if(typeof this.suspendData === "undefined") this.suspendData =(this.verScorm=="2004")?doGetValue("cmi.suspend_data"):doLMSGetValue("cmi.suspend_data");
	return this.suspendData;
};
RTE.prototype.setSuspendData = function(data){
	var ret =(this.verScorm=="2004")?doSetValue("cmi.suspend_data", data):doLMSSetValue("cmi.suspend_data", data);
	if (ret) this.suspendData=data;
	return ret;
};
RTE.prototype.getCompletionStatus = function(){
	if(typeof this.suspendData === "undefined") this.completionStatus =(this.verScorm=="2004")?doGetValue("cmi.completion_status"):doLMSGetValue("cmi.core.lesson_status");
	return this.completionStatus;
};
RTE.prototype.setCompletionStatus = function(completion){
	var ret =(this.verScorm=="2004")?doSetValue("cmi.completion_status", completion):doLMSSetValue("cmi.core.lesson_status", completion);
	if (ret) this.completionStatus=completion;
	return ret;
};
RTE.prototype.getScoreRaw = function(){
	if(typeof this.scoreRaw === "undefined") this.scoreRaw =(this.verScorm=="2004")?doGetValue("cmi.score.raw"):doLMSGetValue("cmi.core.score.raw");
	return this.scoreRaw;
};
RTE.prototype.setScoreRaw = function(raw){
	var ret =(this.verScorm=="2004")?doSetValue("cmi.score.raw", raw):doLMSSetValue("cmi.core.score.raw", raw);
	if (ret) this.scoreRaw=raw;
	return ret;
};
RTE.prototype.getScoreMin = function(){
	if(typeof this.scoreMin === "undefined") this.scoreMin =(this.verScorm=="2004")?doGetValue("cmi.score.min"):doLMSGetValue("cmi.core.score.min");
	return this.scoreMin;
};
RTE.prototype.setScoreMin = function(scoremin){
	var ret =(this.verScorm=="2004")?doSetValue("cmi.score.min", scoremin):doLMSSetValue("cmi.core.score.min", scoremin);
	if (ret) this.scoreMin=scoremin;
	return ret;
};
RTE.prototype.getScoreMax = function(){
	if(typeof this.scoreMax === "undefined") this.scoreMax =(this.verScorm=="2004")?doGetValue("cmi.score.max"):doLMSGetValue("cmi.core.score.max");
	return this.scoreMax;
};
RTE.prototype.setScoreMax = function(scoremax){
	var ret =(this.verScorm=="2004")?doSetValue("cmi.score.max", scoremax):doLMSSetValue("cmi.core.score.max", scoremax);
	if (ret) this.scoreMax=scoremax;
	return ret;
};
RTE.prototype.getLearnerPreferenceAudio = function(){
	if(typeof this.learnerPreferenceAudio === "undefined") this.learnerPreferenceAudio =(this.verScorm=="2004")?doGetValue("cmi.learner_preference.audio_level"):doLMSGetValue("cmi.student_preference.audio");
	return this.learnerPreferenceAudio;
};
RTE.prototype.setLearnerPreferenceAudio = function(audio){
	var ret =(this.verScorm=="2004")?doSetValue("cmi.learner_preference.audio_level", audio):doLMSSetValue("cmi.student_preference.audio", audio);
	if (ret) this.learnerPreferenceAudio=audio;
	return ret;
};
RTE.prototype.getLearnerPreferenceLanguage = function(){
	if(typeof this.learnerPreferenceLanguage === "undefined") this.learnerPreferenceLanguage =(this.verScorm=="2004")?doGetValue("cmi.learner_preference.language"):doLMSGetValue("cmi.student_preference.language");
	return this.learnerPreferenceLanguage;
};
RTE.prototype.setLearnerPreferenceLanguage = function(language){
	var ret =(this.verScorm=="2004")?doSetValue("cmi.learner_preference.language", language):doLMSSetValue("cmi.student_preference.language", language);
	if (ret) this.learnerPreferenceLanguage=language;
	return ret;
};
RTE.prototype.getLearnerPreferenceSpeed = function(){
	if(typeof this.learnerPreferenceSpeed === "undefined") this.learnerPreferenceSpeed =(this.verScorm=="2004")?doGetValue("cmi.learner_preference.delivery_speed"):doLMSGetValue("cmi.student_preference.speed");
	return this.learnerPreferenceSpeed;
};
RTE.prototype.setLearnerPreferenceSpeed = function(speed){
	var ret =(this.verScorm=="2004")?doSetValue("cmi.learner_preference.delivery_speed", speed):doLMSSetValue("cmi.student_preference.speed", speed);
	if (ret) this.learnerPreferenceSpeed=speed;
	return ret;
};
RTE.prototype.getLearnerPreferenceCaptioning = function(){
	if(typeof this.learnerPreferenceCaptioning === "undefined") this.learnerPreferenceCaptioning =(this.verScorm=="2004")?doGetValue("cmi.learner_preference.audio_captioning"):doLMSGetValue("cmi.student_preference.text");
	return this.learnerPreferenceCaptioning;
};
RTE.prototype.setLearnerPreferenceCaptioning = function(captioning){
	var ret =(this.verScorm=="2004")?doSetValue("cmi.learner_preference.audio_captioning", captioning):doLMSSetValue("cmi.student_preference.text", captioning);
	if (ret) this.learnerPreferenceCaptioning=captioning;
	return ret;
};
RTE.prototype.setLearnerPreferences = function(audio, language, speed, captioning){
	var aux0=this.setLearnerPreferenceAudio(audio);
	var aux1=this.setLearnerPreferenceLanguage(language);
	var aux2=this.setLearnerPreferenceSpeed(speed);
	var aux3=this.setLearnerPreferenceCaptioning(captioning);
	return aux0&&aux1&&aux2&&aux3;
};
RTE.prototype.getProgressMeasure = function(){ // SCORM2004v4 exclusive function
	if(typeof this.progressMeasure === "undefined") this.progressMeasure = doGetValue("cmi.progress_measure");
	return this.progressMeasure;
};
RTE.prototype.setProgressMeasure = function(progress){ // SCORM2004v4 exclusive function
	var ret = doSetValue("cmi.progress_measure", progress);
	if (ret) this.progressMeasure=progress;
	return ret;
};
RTE.prototype.getSuccessStatus = function(){ // SCORM2004v4 exclusive function
	this.successStatus = doGetValue("cmi.success_status");
	return this.successStatus;
};
RTE.prototype.setSuccessStatus = function(success){ // SCORM2004v4 exclusive function
	var ret = doSetValue("cmi.success_status", success);
	if (ret) this.successStatus=success;
	return ret;
};
RTE.prototype.getScoreScaled = function(){ // SCORM2004v4 exclusive function
	if(typeof this.scoreScaled === "undefined") this.scoreScaled = doGetValue("cmi.score.scaled");
	return this.scoreScaled;
};
RTE.prototype.setScoreScaled = function(score_scaled){ // SCORM2004v4 exclusive function
	var ret = doSetValue("cmi.score.scaled", score_scaled);
	if (ret) this.scoreScaled=score_scaled;
	return ret;
};
RTE.prototype.getNavRequest = function(){ // SCORM2004v4 exclusive function
	this.navRequest = doGetValue("adl.nav.request");
	return this.navRequest;
};
RTE.prototype.setNavRequest = function(nav_req){ // SCORM2004v4 exclusive function
	var ret = doSetValue("adl.nav.request", nav_req);
	if (ret) this.navRequest=nav_req;
	return ret;
};
/* ***************************** Write only variables*/
RTE.prototype.setExit = function(exit){
	var ret =(this.verScorm=="2004")?doSetValue("cmi.exit", exit):doLMSSetValue("cmi.core.exit", exit);
	return ret;
};
RTE.prototype.setSessionTime = function(){
	var currentMS = this.getWorkingSeconds(this.startMS);
	currentMS = this.getLatencyFromSeconds(currentMS);
	var ret =(this.verScorm=="2004")?doSetValue("cmi.session_time", currentMS):doLMSSetValue("cmi.core.session_time", currentMS);
	return ret;
};
/* ***************************** Comments from learner functions */
/*SCORM2004v4 exclusive function*/
RTE.prototype.getCommentsLearner = function(i){
	this.commentsLearner[i]=doGetValue("cmi.comments_from_learner." + i + ".comment");
	return this.commentsLearner[i];
};
RTE.prototype.getCommentsLearnerLoc = function(i){
	this.commentsLearnerLoc[i]=doGetValue("cmi.comments_from_learner." + i + ".location");
	return this.commentsLearnerLoc[i];
};
RTE.prototype.getCommentsLearnerTs = function(i){
	this.commentsLearnerTs[i]=doGetValue("cmi.comments_from_learner." + i + ".timestamp");
	return this.commentsLearnerTs[i];
};
RTE.prototype.getCommentsLearnerAll = function(i){
	var aux=new Array();
	aux[0]=this.getCommentsLearner(i);
	aux[1]=this.getCommentsLearnerLoc(i);
	aux[2]=this.getCommentsLearnerTs(i);
	return aux;
};
RTE.prototype.setCommentsLearner = function(i,comment){
	var ret = doSetValue("cmi.comments_from_learner." + i + ".comment", comment);
	if (ret) {
		this.commentsLearner[i]=comment;
		this.commentsLearnerCount=this.getCommentsLearnerCount();
	}
	return ret;
};
RTE.prototype.setCommentsLearnerLoc = function(i,loc){
	var ret = doSetValue("cmi.comments_from_learner." + i + ".location", loc);
	if (ret) {
		this.commentsLearnerLoc[i]=loc;
		this.commentsLearnerCount=this.getCommentsLearnerCount();
	}
	return ret;
};
RTE.prototype.setCommentsLearnerTs = function(i){
	var ts=timeStamp(this.verScorm);
	var ret = doSetValue("cmi.comments_from_learner." + i + ".timestamp", ts);
	if (ret) {
		this.commentsLearnerTs[i]=ts;
		this.commentsLearnerCount=this.getCommentsLearnerCount();
	}
	return ret;
};
RTE.prototype.setCommentsLearnerAll = function(i, comment, loc){
	var aux0=this.setCommentsLearner(i, comment);
	var aux1=this.setCommentsLearnerLoc(i, loc);
	var aux2=this.setCommentsLearnerTs(i);
	return aux0&&aux1&&aux2;
};
RTE.prototype.setCommentsLearnerAddAll = function(comment, loc){
	var i=this.getCommentsLearnerCount();
	var aux=this.setCommentsLearnerAll(i, comment, loc);
	return aux;
};
/*SCORM1.2 exclusive functions*/
RTE.prototype.getCommentsLearner = function(){ // SCORM1.2 exclusive function
	if(typeof this.commentsLearner === "undefined") this.commentsLearner = doLMSGetValue("cmi.comments");
	return this.commentsLearner;
};
RTE.prototype.setCommentsLearner = function(comment){ // SCORM1.2 exclusive function
	var aux=doLMSSetValue("cmi.comments", comment);
	return aux;
};
/* ***************************** Comments from LMS functions */
/*SCORM2004v4 exclusive function*/
RTE.prototype.getCommentsLMS = function(i){
	this.commentsLMS[i]=doGetValue("cmi.comments_from_LMS." + i + ".comment");
	return this.commentsLMS[i];
};
RTE.prototype.getCommentsLMSLoc = function(i){
	this.commentsLMSLoc[i]=doGetValue("cmi.comments_from_LMS." + i + ".location");
	return this.commentsLMSLoc[i];
};
RTE.prototype.getCommentsLMSTs = function(i){
	this.commentsLMSTs[i]=doGetValue("cmi.comments_from_LMS." + i + ".timestamp");
	return this.commentsLMSTs[i];
};
RTE.prototype.getCommentsLMSAll = function(i){
	var aux=new Array();
	aux[0]=this.getCommentsLMS(i);
	aux[1]=this.getCommentsLMSLoc(i);
	aux[2]=this.getCommentsLMSTs(i);
	return aux;
};
/*SCORM1.2 exclusive function*/
RTE.prototype.getCommentsLMS = function(){ // SCORM1.2 exclusive function
	if(typeof this.commentsLMS === "undefined") this.commentsLMS = doLMSGetValue("cmi.comments_from_lms");
	return this.commentsLMS;
};
/* ***************************** Interactions functions */
RTE.prototype.getInteractionsObjectivesCount = function(i){
	this.interactionsObjectivesCount[i]=(this.verScorm=="2004")?doGetValue("cmi.interactions." + i + ".objectives._count"):doGetValue("cmi.interactions." + i + ".objectives._count");
	return this.interactionsObjectivesCount[i];
};
RTE.prototype.getInteractionsCorrectRespCount = function(i){
	this.interactionsCorrectRespCount[i]=(this.verScorm=="2004")?doGetValue("cmi.interactions." + i + ".correct_responses._count"):doGetValue("cmi.interactions." + i + ".correct_responses._count");
	return this.interactionsCorrectRespCount[i];
};
RTE.prototype.getInteractionsId = function(i){ // SCORM2004v4 exclusive function
	this.interactionsId[i]=doGetValue("cmi.interactions." + i + ".id");
	return this.interactionsId[i];
};
RTE.prototype.getInteractionsType = function(i){ // SCORM2004v4 exclusive function
	this.interactionsType[i]=doGetValue("cmi.interactions." + i + ".type");
	return this.interactionsType[i];
};
RTE.prototype.getInteractionsObjectivesId = function(i, j){ // SCORM2004v4 exclusive function
	if (typeof this.interactionsObjectivesId[i] === "undefined") this.interactionsObjectivesId[i] = new Array();
	this.interactionsObjectivesId[i][j]=doGetValue("cmi.interactions." + i + ".objectives." + j + ".id");
	return this.interactionsObjectivesId[i][j];
};
RTE.prototype.getInteractionsTs = function(i){ // SCORM2004v4 exclusive function
	this.interactionsTs[i]=doGetValue("cmi.interactions." + i + ".timestamp");
	return this.interactionsTs[i];
};
RTE.prototype.getInteractionsCorrectRespPattern = function(i, j){ // SCORM2004v4 exclusive function
	if (typeof this.interactionsCorrectRespPattern[i] === "undefined") this.interactionsCorrectRespPattern[i] = new Array();
	this.interactionsCorrectRespPattern[i][j]=doGetValue("cmi.interactions." + i + ".correct_responses." + j + ".pattern");
	return this.interactionsCorrectRespPattern[i][j];
};
RTE.prototype.getInteractionsWeight = function(i){ // SCORM2004v4 exclusive function
	this.interactionsWeight[i]=doGetValue("cmi.interactions." + i + ".weighting");
	return this.interactionsWeight[i];
};
RTE.prototype.getInteractionsResponse = function(i){ // SCORM2004v4 exclusive function
	this.interactionsResponse[i]=doGetValue("cmi.interactions." + i + ".learner_response");
	return this.interactionsResponse[i];
};
RTE.prototype.getInteractionsResult = function(i){ // SCORM2004v4 exclusive function
	this.interactionsResult[i]=doGetValue("cmi.interactions." + i + ".result");
	return this.interactionsResult[i];
};
RTE.prototype.getInteractionsLatency = function(i){ // SCORM2004v4 exclusive function
	this.interactionsLatency[i]=doGetValue("cmi.interactions." + i + ".latency");
	return this.interactionsLatency[i];
};
RTE.prototype.getInteractionsDescription = function(i){ // SCORM2004v4 exclusive function
	this.interactionsDescription[i]=doGetValue("cmi.interactions." + i + ".description");
	return this.interactionsDescription[i];
};
RTE.prototype.getInteractionsAll = function(i){ // SCORM2004v4 exclusive function
	var aux=new Array();
	aux[0]=this.getInteractionsId(i);
	aux[1]=this.getInteractionsType(i);
	aux[2]=this.getInteractionsTs(i);
	aux[3]=this.getInteractionsWeight(i);
	aux[4]=this.getInteractionsResponse(i);
	aux[5]=this.getInteractionsResult(i);
	aux[6]=this.getInteractionsLatency(i);
	aux[7]=this.getInteractionsDescription(i);
	aux[8]=this.getInteractionsObjectivesCount(i);
	aux[9]=this.getInteractionsCorrectRespCount(i);
	var j=0;
	if (aux[8]!=0){
		for (j=0;j<aux[8];j++){
			aux[10+j]=this.getInteractionsObjectivesId(i,j);
		}
	}
	if (aux[9]!=0){
		for (k=0;k<aux[9];k++){
			aux[10+j+k]=this.getInteractionsCorrectRespPattern(i,k);
		}
	}
	return aux;
};
RTE.prototype.setInteractionsId = function(i, id){
	var ret =(this.verScorm=="2004")?doSetValue("cmi.interactions." + i + ".id", id):doLMSSetValue("cmi.interactions." + i + ".id", id);
	if (ret){
		var startDate = new Date();
		this.interactionsInitTime[i]=startDate.getTime();
		this.interactionsId[i]=id;
		this.interactionsCount=this.getInteractionsCount();
	}
	return ret;
};
RTE.prototype.setInteractionsType = function(i, type){
	var ret =(this.verScorm=="2004")?doSetValue("cmi.interactions." + i + ".type", type):doLMSSetValue("cmi.interactions." + i + ".type", type);
	if (ret) this.interactionsType[i]=type;
	return ret;
};
RTE.prototype.setInteractionsObjectivesId = function(i, j, id){
	if (typeof(this.interactionsObjectivesId[i])=="undefined") this.interactionsObjectivesId[i]=new Array();
	var ret =(this.verScorm=="2004")?doSetValue("cmi.interactions." + i + ".objectives." + j + ".id", id):doLMSSetValue("cmi.interactions." + i + ".objectives." + j + ".id", id);
	if (ret) {
		this.interactionsObjectivesId[i][j]=id;
		this.interactionsObjectivesCount[i]=this.getInteractionsObjectivesCount(i);		
	}
	return ret;
};
RTE.prototype.setInteractionsTs = function(i, ts){
	var ret =(this.verScorm=="2004")?doSetValue("cmi.interactions." + i + ".timestamp", ts):doLMSSetValue("cmi.interactions." + i + ".time", ts);;
	if (ret) this.interactionsTs[i]=ts;
	return ret;
};
RTE.prototype.setInteractionsTsAut = function(i){
	var ret =(this.verScorm=="2004")?doSetValue("cmi.interactions." + i + ".timestamp", timeStamp(this.verScorm)):doLMSSetValue("cmi.interactions." + i + ".time", timeStamp("1.2"));
	if (ret) this.interactionsTs[i]=timeStamp(this.verScorm);
	return ret;
};
RTE.prototype.setInteractionsCorrectRespPattern = function(i, j, respCorr){
	if (typeof(this.interactionsCorrectRespPattern[i])=="undefined") this.interactionsCorrectRespPattern[i]=new Array();
	var ret =(this.verScorm=="2004")?doSetValue("cmi.interactions." + i + ".correct_responses." + j + ".pattern", respCorr):doLMSSetValue("cmi.interactions." + i + ".correct_responses." + j + ".pattern", respCorr);
	if (ret) this.interactionsCorrectRespPattern[i][j]=respCorr;
	return ret;
};
RTE.prototype.setInteractionsWeight = function(i, weighting){
	var ret =(this.verScorm=="2004")?doSetValue("cmi.interactions." + i + ".weighting", weighting):doLMSSetValue("cmi.interactions." + i + ".weighting", weighting);
	if (ret) this.interactionsWeight[i]=weighting;
	return ret;
};
RTE.prototype.setInteractionsResponse = function(i, response){
	var ret =(this.verScorm=="2004")?doSetValue("cmi.interactions." + i + ".learner_response", response):doLMSSetValue("cmi.interactions." + i + ".student_response", response);
	if (ret) this.interactionsResponse[i]=response;
	return ret;
};
RTE.prototype.setInteractionsResult = function(i, result){
	var ret =(this.verScorm=="2004")?doSetValue("cmi.interactions." + i + ".result", result):doLMSSetValue("cmi.interactions." + i + ".result", result);
	if (ret) this.interactionsResult[i]=result;
	return ret;
};
RTE.prototype.setInteractionsLatency = function(i, latency){
	var ret =(this.verScorm=="2004")?doSetValue("cmi.interactions." + i + ".latency", latency):doLMSSetValue("cmi.interactions." + i + ".latency", latency);
	if (ret) this.interactionsLatency[i]=latency;
	return ret;
};
RTE.prototype.setInteractionsLatencyAut = function(i){
	var elapsedSeconds=this.getWorkingSeconds(this.interactionsInitTime[i]); //Calculate seconds elapsed after store interaction in LMS
	var latency=this.getLatencyFromSeconds(elapsedSeconds); // Convert seconds to SCORM latency format 
	var ret =(this.verScorm=="2004")?doSetValue("cmi.interactions." + i + ".latency", latency):doLMSSetValue("cmi.interactions." + i + ".latency", latency);
	if (ret) this.interactionsLatency[i]=latency;
	return ret;
};
RTE.prototype.setInteractionsAll = function(i, id, type, ts, weighting, pattern, response, result, latency){
	var aux0=this.setInteractionsId(i, id);
	var aux1=this.setInteractionsType(i, type);
	var aux2=this.setInteractionsTs(i, ts);
	var aux3=this.setInteractionsWeight(i, weighting);
	var aux4=this.setInteractionsCorrectRespPattern(i, 0, pattern);
	var aux5=this.setInteractionsResponse(i, response);
	var aux6=this.setInteractionsResult(i, result);
	var aux7=this.setInteractionsLatencyAut(i);
	return aux0&&aux1&&aux2&&aux3&&aux4&&aux5&&aux6&&aux7;
};
RTE.prototype.setInteractionsAddAll = function(id, type, weighting, pattern, response, result){
	var i=this.getInteractionsCount();
	var aux0=this.setInteractionsId(i, id);
	var aux1=this.setInteractionsType(i, type);
	var aux2=this.setInteractionsTs(i, timeStamp(this.verScorm));
	var aux3=this.setInteractionsWeight(i, weighting);
	var aux4=this.setInteractionsCorrectRespPattern(i, 0, pattern);
	var aux5=this.setInteractionsResponse(i, response);
	var aux6=this.setInteractionsResult(i, result);
	var aux7=this.setInteractionsLatencyAut(i);
	return aux0&&aux1&&aux2&&aux3&&aux4&&aux5&&aux6&&aux7;
};
RTE.prototype.setInteractionsAddNew = function(id, type, weighting, pattern){
	var i=this.getInteractionsCount();
	var aux0=this.setInteractionsId(i, id);
	var aux1=this.setInteractionsType(i, type);
	var aux2=this.setInteractionsTs(i, timeStamp(this.verScorm));
	var aux3=this.setInteractionsWeight(i, weighting);
	var aux4=this.setInteractionsCorrectRespPattern(i, 0, pattern);
	return aux0&&aux1&&aux2&&aux3&&aux4;
};
RTE.prototype.setInteractionsResponseAll = function(i, response, result){
	var aux0=this.setInteractionsResponse(i, response);
	var aux1=this.setInteractionsResult(i, result);
	var aux2=this.setInteractionsLatencyAut(i);
	return aux0&&aux1&&aux2;
};
RTE.prototype.setInteractionsDescription = function(i, description){ // SCORM2004v4 exclusive function
	var ret = doSetValue("cmi.interactions." + i + ".description", description);
	if (ret) this.interactionsDescription[i]=description;
	return ret;
};
/* Objectives functions */
RTE.prototype.getObjectivesId = function(i){
	this.objectivesId[i]=(this.verScorm=="2004")?doGetValue("cmi.objectives." + i + ".id"):doLMSGetValue("cmi.objectives." + i + ".id");
	return this.objectivesId[i];
};
RTE.prototype.getObjectivesScoreRaw = function(i){
	this.objectivesScoreRaw[i]=(this.verScorm=="2004")?doGetValue("cmi.objectives." + i + ".score.raw"):doLMSGetValue("cmi.objectives." + i + ".score.raw");
	return this.objectivesScoreRaw[i];
};
RTE.prototype.getObjectivesScoreMin = function(i){
	this.objectivesScoreMin[i]=(this.verScorm=="2004")?doGetValue("cmi.objectives." + i + ".score.min"):doLMSGetValue("cmi.objectives." + i + ".score.min");
	return this.objectivesScoreMin[i];
};
RTE.prototype.getObjectivesScoreMax = function(i){
	this.objectivesScoreMax[i]=(this.verScorm=="2004")?doGetValue("cmi.objectives." + i + ".score.max"):doLMSGetValue("cmi.objectives." + i + ".score.max");
	return this.objectivesScoreMax[i];
};
RTE.prototype.getObjectivesCompletionStatus = function(i){
	this.objectivesCompletionStatus[i]=(this.verScorm=="2004")?doGetValue("cmi.objectives." + i + ".completion_status"):doLMSGetValue("cmi.objectives." + i + ".status");
	return this.objectivesCompletionStatus[i];
};
RTE.prototype.getObjectivesAll = function(i){
	var aux=new Array();
	aux[0]=this.getObjectivesId(i);
	aux[1]=this.getObjectivesScoreRaw(i);
	aux[2]=this.getObjectivesScoreMin(i);
	aux[3]=this.getObjectivesScoreMax(i);
	aux[4]=this.getObjectivesCompletionStatus(i);
	return aux;
};
RTE.prototype.getObjectivesAll04 = function(i){ //SCORM2004v4 exclusive function
	var aux=new Array();
	aux[0]=this.getObjectivesId(i);
	aux[1]=this.getObjectivesScoreScaled(i);
	aux[2]=this.getObjectivesScoreRaw(i);
	aux[3]=this.getObjectivesScoreMin(i);
	aux[4]=this.getObjectivesScoreMax(i);
	aux[5]=this.getObjectivesSuccessStatus(i);
	aux[6]=this.getObjectivesCompletionStatus(i);
	aux[7]=this.getObjectivesProgressMeasure(i);
	aux[8]=this.getObjectivesDescription(i);
	return aux;
};
RTE.prototype.getObjectivesScoreScaled = function(i){ //SCORM2004v4 exclusive function
	this.objectivesScoreScaled[i]=doGetValue("cmi.objectives." + i + ".score.scaled");
	return this.objectivesScoreScaled[i];
};
RTE.prototype.getObjectivesSuccessStatus = function(i){ //SCORM2004v4 exclusive function
	this.objectivesSuccessStatus[i]=doGetValue("cmi.objectives." + i + ".success_status");
	return this.objectivesSuccessStatus[i];
};
RTE.prototype.getObjectivesProgressMeasure = function(i){ //SCORM2004v4 exclusive function
	this.objectivesProgressMeasure[i]=doGetValue("cmi.objectives." + i + ".progress_measure");
	return this.objectivesProgressMeasure[i];
};
RTE.prototype.getObjectivesDescription = function(i){ //SCORM2004v4 exclusive function
	this.objectivesDescription[i]=doGetValue("cmi.objectives." + i + ".description");
	return this.objectivesDescription[i];
};
RTE.prototype.setObjectivesId = function(i, id){
	var ret =(this.verScorm=="2004")?doSetValue("cmi.objectives." + i + ".id", id):doLMSSetValue("cmi.objectives." + i + ".id", id);
	if (ret) {
		this.objectivesId[i]=id;
		this.objectivesCount=this.getObjectivesCount();
	}
	return ret;
};
RTE.prototype.setObjectivesScoreRaw = function(i, scoreraw){
	var ret =(this.verScorm=="2004")?doSetValue("cmi.objectives." + i + ".score.raw", scoreraw):doLMSSetValue("cmi.objectives." + i + ".score.raw", scoreraw);
	if (ret) this.objectivesScoreRaw[i]=scoreraw;
	return ret;
};
RTE.prototype.setObjectivesScoreMin = function(i, scoremin){
	var ret =(this.verScorm=="2004")?doSetValue("cmi.objectives." + i + ".score.min", scoremin):doLMSSetValue("cmi.objectives." + i + ".score.min", scoremin);
	if (ret) this.objectivesScoreMin[i]=scoremin;
	return ret;
};
RTE.prototype.setObjectivesScoreMax = function(i, scoremax){
	var ret =(this.verScorm=="2004")?doSetValue("cmi.objectives." + i + ".score.max", scoremax):doLMSSetValue("cmi.objectives." + i + ".score.max", scoremax);
	if (ret) this.objectivesScoreMax[i]=scoremax;
	return ret;
};
RTE.prototype.setObjectivesCompletionStatus = function(i, completion){
	var ret =(this.verScorm=="2004")?doSetValue("cmi.objectives." + i + ".completion_status", completion):doLMSSetValue("cmi.objectives." + i + ".status", completion);
	if (ret) this.objectivesCompletionStatus[i]=completion;
	return ret;
};
RTE.prototype.setObjectivesAll = function(i, id, scoreraw, scoremin, scoremax, completion){
	var aux0=this.setObjectivesId(i, id);
	var aux1=this.setObjectivesScoreRaw(i, scoreraw);
	var aux2=this.setObjectivesScoreMin(i, scoremin);
	var aux3=this.setObjectivesScoreMax(i, scoremax);
	var aux4=this.setObjectivesCompletionStatus(i, completion);
	return aux0&&aux1&&aux2&&aux3&&aux4;
};
RTE.prototype.setObjectivesAddAll = function(id, scoreraw, scoremin, scoremax, completion){
	var i=this.getObjectivesCount();
	var aux=this.setObjectivesAll(i, id, scoreraw, scoremin, scoremax, completion);
	return aux;
};
RTE.prototype.setObjectivesScoreScaled = function(i, scorescaled){ //SCORM2004v4 exclusive function
	var ret = doSetValue("cmi.objectives." + i + ".score.scaled", scorescaled);
	if (ret) this.objectivesScoreScaled[i]=scorescaled;
	return ret;
};
RTE.prototype.setObjectivesSuccessStatus = function(i, success){ //SCORM2004v4 exclusive function
	var ret = doSetValue("cmi.objectives." + i + ".success_status", success);
	if (ret) this.objectivesSuccessStatus[i]=success;
	return ret;
};
RTE.prototype.setObjectivesProgressMeasure = function(i, progressm){ //SCORM2004v4 exclusive function
	var ret = doSetValue("cmi.objectives." + i + ".progress_measure", progressm);
	if (ret) this.objectivesProgressMeasure[i]=progressm;
	return ret;
};
RTE.prototype.setObjectivesDescription = function(i, description){ //SCORM2004v4 exclusive function
	var ret = doSetValue("cmi.objectives." + i + ".description", description);
	if (ret) this.objectivesDescription[i]=description;
	return ret;
};
RTE.prototype.setObjectivesAll04 = function(i, id, scorescaled, scoreraw, scoremin, scoremax, success, completion, progressm, description){ //SCORM2004v4 exclusive function
	var aux0=this.setObjectivesId(i, id);
	var aux1=this.setObjectivesScoreScaled(i, scorescaled);
	var aux2=this.setObjectivesScoreRaw(i, scoreraw);
	var aux3=this.setObjectivesScoreMin(i, scoremin);
	var aux4=this.setObjectivesScoreMax(i, scoremax);
	var aux5=this.setObjectivesSuccessStatus(i, success);
	var aux6=this.setObjectivesCompletionStatus(i, completion);
	var aux7=this.setObjectivesProgressMeasure(i, progressm);
	var aux8=this.setObjectivesDescription(i, description);
	return aux0&&aux1&&aux2&&aux3&&aux4&&aux5&&aux6&&aux7&&aux8;
};
RTE.prototype.setObjectivesAddAll04 = function(id, scorescaled, scoreraw, scoremin, scoremax, success, completion, progressm, description){ //SCORM2004v4 exclusive function
	var i=this.getObjectivesCount();
	var aux=this.setObjectivesAll04(i, id, scorescaled, scoreraw, scoremin, scoremax, success, completion, progressm, description);
	return aux;
};
/* Shared Data functions  --SCORM2004v4 exclusive functions--*/
RTE.prototype.getDataId = function(i){
	this.dataId[i]=doGetValue("adl.data." + i + ".id");
	return this.dataId[i];
};
RTE.prototype.getDataStore = function(i){
	this.dataStore[i]=doGetValue("adl.data." + i + ".store");
	return this.dataStore[i];
};
RTE.prototype.setDataStore = function(i, data){
	var ret = doSetValue("adl.data." + i + ".store", data);
	if (ret) this.dataStore[i]=data;
	return ret;
};
RTE.prototype.getDataAll = function(i){
	var aux=Array();
	aux[0]=this.getDataId(i);
	aux[1]=this.getDataStore(i);
	return aux;
};