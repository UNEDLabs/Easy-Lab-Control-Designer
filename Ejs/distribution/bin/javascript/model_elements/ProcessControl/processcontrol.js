/**
 * @author Jesús Chacón <jchacon@bec.uned.es>
 */

class StateSpaceModel {
  constructor() {
  	this.A = [[0, 1],[0, 0]];
  	this.B = [[0], [1]];
   	this.C = [[0, 1]];
  	this.D = [[0]];
  }

  // Modify the system matrices
  setModel(A, B, C, D) {
//		if(!this.isSquare(A)	|| !this.hasRowSize(B, A.length)
//		|| !this.hasColumnSize(C, A.length))
//		  return;
//		setNumberOfStates(A.length);
//		setNumberOfInputs(B[0].length);
//		setNumberOfOutputs(C.length);
  		this.A = A.slice();
  		this.B = B.slice();
  		this.C = C.slice();
  		this.D = D.slice();
  }

//StateSpaceModel.prototype.hasRowSize = function(A, p) {
//  return this.hasDimension(A, p, 1);
//};

//StateSpaceModel.prototype.hasColumnSize = function(A, p) {
//  return this.hasDimension(A, 1, q);
//};

//StateSpaceModel.prototype.isSquare = function(A) {
//	if(A == undefined) return false;
//	var n = A.length;
//	return this.hasDimension(A, n, n);
//};

//// Checks if the size of the matrix A is p x q
//StateSpaceModel.prototype.hasDimension = function(A, p, q) {
//	if(A == undefined) return false;
//	var isValid = true;
//	// does all the rows have the same dimension?
//	var m = A.length, n = A[0].length;
//	for(var i=1; i<m; i++) {
//	  if(!Array.isArray(A[m]) || A[m].length != m) {
//	    isValid = false;
//	    break;
//	  }
//	}		
//	// is the matrix square?
//	if(m != n) isValid = false;
//	return isValid;
//};

/* 
 * Interface Continuous 	
 */

  // Returns the derivatives of the states of system as dx(t)=A*x(t)+B*u(t), for the given values of x and u.
  getRates(x, u) {
  	var nstates = this.getStateSize(),
  	    ninputs = this.getInputSize();
  	var dx = [];
  	for(var i=0; i<nstates; i++) {
  		dx[i] = 0;
  		for(var j=0; j<nstates; j++) {
    		dx[i] += this.A[i][j]*x[j];
  		}
  		for(var j=0; j<ninputs; j++) {
  		  dx[i] += this.B[i][j]*u[j];
  		}
  	}
    return dx;
  }

  getStateSize() {
    return (this.A != undefined) ? this.A.length : 0;
  }

  getInputSize() {
    return (this.B != undefined) ? this.B[0].length : 0;
  }

  getOutputSize() {
  	return (this.C != undefined) ? this.C.length : 0;
  }

  // Returns the output of the system as y(t)=C*x(t)+D*u(t), for the given values of x and u.
  getOutput(x, u) {
  	var nstates = this.getStateSize(),
  	    ninputs = this.getInputSize(),
  	    noutputs = this.getOutputSize();

//  	var noutputs = (this.C != undefined) ? this.C.length : 0;
//  	nstates = x.length;


  	var y = [];
  	for(var i=0; i<noutputs; i++) {
  		y[i] = 0;
  		for(var j=0; j<nstates; j++) {
  		  y[i] += this.C[i][j]*x[j];
 		  }
  		for(var j=0; j<ninputs; j++) {
  		  y[i] += this.D[i][j]*u[j];
  		}
  	}
  	return y;
  }

/*StateSpaceModel.prototype.getOutput = function(i, x, u) {
//	if(i < 0 || i > noutputs) throw new ArrayIndexOutOfBoundsException();
	var y = 0;
	var ninputs = (u != null) ? u.length : 0;
	if(ninputs > this.ninputs) ninputs = this.ninputs;
	var nstates = x.length;
	for(var j=0; j<nstates; j++) y += this.C[i][j]*x[j];
	for(var j=0; j<ninputs; j++) y += this.D[i][j]*u[j];
	return y;
};*/
}
/**
 * 
 * @author jcsombria
 *
 *  PidController
 *
 *	Class to implement a continuous PID controller.
 *               ________
 *	setpoint ---|        |
 *	output   ---| PID(s) |---- control action
 *	tracking ---|________|
 */
class PIDController extends StateSpaceModel {
  constructor() {
    super();
    // default params (kp, ki, kd, n, ks)
    this.setParameters(1.0, 1.0, 1.0, 2.0, 1.0);
    // default range (uMin, uMax)
    this.setRange(0.0, 1.0);
	
    this.setpoint = 0.0;
    this.antiwindup = false;
    this.tracking = false;
  }

  // PID configuration
  setParameters(kp, ki, kd, n, ks) {
    this.setN(n);
    this.setKs(ks);
    this.setKp(kp);
    this.setKi(ki);
    this.setKd(kd);
	}

  setKs(ks) {
		this.ks = (ks > 0) ? ks : 0;
  }

  setN(n) {
		this.n = (n > 0) ? n : 0;		
  }

  setKp(kp) {	
 		this.kp = (kp > 0) ? kp : 0;    
  }
  
  setKi(ki) {
		this.ki = (ki > 0) ? ki : 0;
  }

  setTi(ti) {
    this.setKi(1/this.ti);
  }

  setKd(kd) {
		this.kd = (kd > 0) ? kd : 0;
		if(this.kd > 0) {
			this.setModel(
			  [[0, 0], [0, -this.n/this.kd]],
			  [[1, -1], [this.kd*this.n, -this.kd*this.n]],
			  [[this.ki, this.kd]],
			  [[this.kp + this.kd*this.n, -this.kp - this.kd*this.n]]);
		} else {
			this.setModel(
			  [[0, 0], [0, 0]],
			  [[1, -1], [0, 0]],
			  [[this.ki, 0]],
			  [[this.kp, -this.kp]]);
		}
  }

  setTd(td) {
    this.setKd(1/this.td);
  }

  setAntiwindup(enabled) {
    this.antiwindup = enabled;
  }

  setTracking(enabled) {
    this.tracking = enabled;
  }

  setUMax(uMax) {
    this.uMax = uMax;
  }

  setUMin(uMin) {
    this.uMin = uMin;
  }

  setRange(uMin, uMax) {
  	this.uMin = uMin;
  	this.uMax = uMax;
  }

  // Compute the derivative of the state.
  getRates(x, u) {
    var dx = super.getRates(x, [u[0], u[1]]), y = super.getOutput(x, [u[0], u[1]]);		

  	if(this.antiwindup) {
  		var v = this.coerce(y[0]);
  		dx[0] += this.ks*(v - y[0]);
  	}
  	if(this.tracking) {
  		if(u != undefined && u.length > 2) {
  		  dx[0] += this.ks*(u[2] - y[0]);
  		}
  	}
  	return dx;
  }

  coerce(u) {
  	return (u < this.uMin) ? this.uMin : (u > this.uMax) ? this.uMax : u;
  } 
}

/**
 * 
 * @author jcsombria
 *
 *  PidController
 *
 *	Class to implement a continuous PID controller.
 *               ________
 *	setpoint ---|        |
 *	output   ---| PID(s) |---- control action
 *	tracking ---|________|
 */
class StateFeedbackController {
  constructor() {
    this.setGains([1]);
  }

  setGains(K) {
		if(K == undefined) return;
		this.K = K;
	}

//  update(x, u) {
//		var n = x.length;
//		if(n != K.length) return;		
//		x[0] = 0; 
//		for(var i=0; i<n; i++) {
//			x[0] += K[i]*u[i];
//		}
//	}

  getOutput(x, u) {
		var n = u.length;
		var y = 0;
		for(var i=0; i<n; i++) {
			y += this.K[i]*u[i];
		}
		return [y];
	}
}
