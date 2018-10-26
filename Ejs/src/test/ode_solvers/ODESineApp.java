package test.ode_solvers;

import java.awt.Color;

import org.opensourcephysics.display.Dataset;
import org.opensourcephysics.display.DrawingFrame;
import org.opensourcephysics.display.PlottingPanel;
import org.opensourcephysics.numerics.ODE;
import org.opensourcephysics.numerics.ode_interpolation.StateHistory;
import org.opensourcephysics.numerics.ode_solvers.InterpolatorEventSolver;
import org.opensourcephysics.numerics.ode_solvers.StateEvent;
import org.opensourcephysics.numerics.ode_solvers.rk.*;

/**
 * Example of direct use of an InterpolatorEventSolver
 * 
 * @author Francisco Esquembre
 * @version 1.0 December 2013
 */
public class ODESineApp {
  static private final double sABS_TOL = 1.0e-6, sREL_TOL = 1.0e-6; // The tolerance for adaptive methods

  static public void main (String[] args) {

    // ODE
    ODE ode = new ODESine();
    double initTime = 0, maxTime = 4*Math.PI;

    // Solver and its parameters
    InterpolatorEventSolver solver = new InterpolatorEventSolver(new Radau5(), ode);

    double stepSize = 0.1; // The initial step size (used by fixed step methods)
    double plotStepSize = 0.1; // The step size for plotting the solution

    // Initialize and customize the solver
    solver.initialize(stepSize);       // This step size affects the solver internal step size
    solver.setStepSize(plotStepSize);  // This step size is the reading step size
    solver.setTolerances(sABS_TOL,sREL_TOL);
    solver.setHistoryLength(Double.POSITIVE_INFINITY); // Recall all past values

    // Add event to solver
    solver.addEvent(new ZeroEvent(ode));

    // main loop for solving the ODE
    double[] state = ode.getState();
    int timeIndex = state.length-1;

    // Solve for the whole [initTime,maxTime] interval at once
    while (solver.getCurrentTime()<maxTime) {
      try{
        solver.step();
        if (solver.getErrorCode()!=InterpolatorEventSolver.ERROR.NO_ERROR) {
          System.err.println ("Error when advancing the solution from "+solver.getCurrentTime()); //$NON-NLS-1$
          break;
        }
      } catch(Exception exc) {
        exc.printStackTrace();
        break;
      }
    }

    // Compute max error at each plot point
    StateHistory history = solver.getStateHistory();

    { // plot the error
      double maxError = 0;
      double maxRelError = 0;
      double[] interpolated = new double[ode.getState().length]; 
      double time = initTime;
      while (time<=maxTime) {
        history.interpolate(time, interpolated);   
        double error = Math.abs(solution(interpolated[timeIndex]) - interpolated[0]);
        double relError=0;
        if (Math.abs(interpolated[0])>InterpolatorEventSolver.EPSILON) relError = error/Math.abs(interpolated[0]);
        System.out.println("Time = " + time + ", value = "+interpolated[0]+ ", Error(t) = " + error + " Relative error(t) = "+relError);
        maxError = Math.max(maxError, error);
        maxRelError = Math.max(maxRelError, relError);
        time += plotStepSize;
      } 
      System.out.println("Max error = " + maxError + " Max relative error = "+maxRelError);
    }


    {  // Plot the graphs
      // Prepare the graphics
      Dataset stripChart = new Dataset(Color.BLUE, Color.BLUE, true);
      stripChart.setMarkerShape(Dataset.NO_MARKER);
      Dataset solutionStripChart = new Dataset(Color.RED, Color.RED, true);
      solutionStripChart.setMarkerShape(Dataset.NO_MARKER);

      double time = initTime;
      double[] interpolated = new double[ode.getState().length]; 
      while (time<=maxTime) {
        history.interpolate(time, interpolated);   
        stripChart.append(time, interpolated[0]);
        solutionStripChart.append(time, solution(time));
        time += plotStepSize;
      }

      PlottingPanel plottingPanel = new PlottingPanel("time", "state", "ODE Test"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      DrawingFrame plottingFrame = new DrawingFrame("ODE Test", plottingPanel); //$NON-NLS-1$
      plottingFrame.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
      plottingPanel.addDrawable(solutionStripChart); // This one is drawn first
      plottingPanel.addDrawable(stripChart);

      plottingPanel.render();
      plottingFrame.setLocation(0, 0);
      plottingFrame.setSize(700,700);
      plottingFrame.setVisible(true);
    }

  }

  static private double solution(double time) {
    return Math.sin(time);
  }

  static private class ODESine implements ODE {
    private double[] mState = {0, 1, 0};

    // Implementation of ODE

    public double[] getState() { return mState; }

    public void getRate(double[] state, double[] rate){    
      rate[0] =  state[1];
      rate[1] = -state[0];
      rate[2] = 1; // time
    }

  } // End of DifferentialEquation

  static private class ZeroEvent implements StateEvent {
    ODE mODE;
    int mEventCounter=0;

    ZeroEvent(ODE ode) {
      mODE = ode;
    }

    public int getTypeOfEvent() {
      return StateEvent.CROSSING_EVENT;
    }

    public double evaluate(double[] state) {
      return state[0];
    }

    public boolean action() {
      mEventCounter++;
      double[] state = mODE.getState();
      double nPi = mEventCounter*Math.PI;
      System.out.print ("zero = "+state[2]); //$NON-NLS-1$
      System.out.print (", "+mEventCounter+"*PI = "+nPi); //$NON-NLS-1$ //$NON-NLS-2$
      System.out.println (", Error = "+Math.abs(state[2]-nPi)); //$NON-NLS-1$
      return false;
    }

    public double getTolerance() {
      return sABS_TOL;
    }

    public int getRootFindingMethod() {
      return StateEvent.SECANT;
    }

    public int getMaxIterations() {
      return 100;
    }

  } // End of StateEvent

}
