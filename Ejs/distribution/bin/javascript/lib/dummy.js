var EJSS_CORE = EJSS_CORE || { };
var EJSS_DRAWING2D = EJSS_DRAWING2D || { };
var EJSS_DRAWING3D = EJSS_DRAWING3D || { };
var EJSS_INTERFACE = EJSS_INTERFACE || { };
var EJSS_ODE_SOLVERS = EJSS_ODE_SOLVERS || { };
var EJSS_TOOLS = EJSS_TOOLS || { };

EJSS_ODE_SOLVERS.rungeKutta4 = function() { return {}; };
EJSS_ODE_SOLVERS.bogackiShampine23 = function() { return {}; };
EJSS_ODE_SOLVERS.cashKarp45 = function() { return {}; };
EJSS_ODE_SOLVERS.dopri5 = function() { return {}; };
EJSS_ODE_SOLVERS.dopri853 = function() { return {}; };
EJSS_ODE_SOLVERS.eulerRichardson = function() { return {}; };
EJSS_ODE_SOLVERS.euler = function() { return {}; };
EJSS_ODE_SOLVERS.fehlberg87 = function() { return {}; };
EJSS_ODE_SOLVERS.fehlberg8 = function() { return {}; };
EJSS_ODE_SOLVERS.radau5 = function() { return {}; };
EJSS_ODE_SOLVERS.velocityVerlet = function() { return {}; };
EJSS_ODE_SOLVERS.interpolatorEventSolver = function(mSolverEngine, mODE) { return {}; };

EJSS_ODE_SOLVERS.EVENT_TYPE = {
  STATE_EVENT : 0,
  POSITIVE_EVENT : 1,
  CROSSING_EVENT : 2
};
  
EJSS_ODE_SOLVERS.EVENT_METHOD = {
  BISECTION : 0,
  SECANT    : 1
};

Math.log10 = function(value) { return Math.log(value)/Math.LN10; };
  
EJSS_ODE_SOLVERS.ERROR = {
    NO_ERROR                     : 0, // Everything went ok
    INTERNAL_SOLVER_ERROR        : 1, // The solver produced an internal error, such as when it cannot do an internal step
    EVENT_NOT_FOUND              : 2, // The event was not found after using the maximum number of attempts
    ILLEGAL_EVENT_STATE          : 3, // An event of type STATE_EVENT was left in an illegal (negative) state
    ZENO_EFFECT                  : 4, // A Zeno effect was detected without a user action
    TOO_MANY_STEPS_ERROR         : 5, // The solver exceeded the number of internal steps
    DISCONTINUITY_PRODUCED_ERROR : 6, // Unrecoverable error produced by a discontinuity
    DID_NOT_CONVERGE             : 7  // An adaptive method did not converge
};

EJSS_ODE_SOLVERS.DISCONTINUITY_CODE = {
    DISCONTINUITY_PRODUCED_ERROR  : 0, // Unrecoverable error
    NO_DISCONTINUITY_ALONG_STEP   : 1, // There is no discontinuity along the given step
    DISCONTINUITY_ALONG_STEP      : 2, // There is a  discontinuity along the given step, but not exactly at its end
    DISCONTINUITY_JUST_PASSED     : 3, // There is a  discontinuity along the given step, and it is just before the current step end
    DISCONTINUITY_EXACTLY_ON_STEP : 4  // There is a  discontinuity along the given step, and its is exactly at the end of the step
};

var window = window || { };
var document = document || { };

var _model = _model || { }; 
var _view = _view || { };
var console = {};
var navigator;
var _isEPub = false;
var _isMobile = false;
var _getODE;
var _getEventSolver;
var alert = function() {};