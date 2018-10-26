package org.colos.roboticsLabs.components;

import org.opensourcephysics.drawing3d.DrawingPanel3D;
import org.opensourcephysics.drawing3d.Group;

public abstract class AbstractComponent {

	// Graphics
	protected boolean mHasViewGroup = false;
	private boolean mAddedToView = false;
	// private Group mComponentGroup, mHostGroup;
	protected Group mComponentGroup, mHostGroup;
	protected double[] mComponentPosition;

	protected AbstractComponent() {}
	
	public double[] getInitialPosition() {
		double[] componentPosition = new double[3];
		System.arraycopy(mComponentPosition, 0, componentPosition, 0, 3);
		return componentPosition;
	}
	
	protected abstract Group createViewGroup();

	public void addToViewGroup(Group group) {
		// System.out.println ("Adding "+this+" to view group = "+group);
		if (mHostGroup != null) {
			mHostGroup.removeElement(mComponentGroup);
			mHostGroup = null;
		}
		if (group == null)
			return;
		if (mComponentGroup == null) {
			mComponentGroup = createViewGroup();
			mAddedToView = true;
			mHostGroup = group;
			mHostGroup.addElement(mComponentGroup);
			DrawingPanel3D panel = mHostGroup.getPanel();
			mComponentGroup.setPanel(panel);
			panel.render();
		}

		mComponentPosition = new double[] { mHostGroup.getX(),
				mHostGroup.getY(), mHostGroup.getZ() };
	}

	public void removeFromViewGroup() {
		if (mHostGroup != null) {
			if (mAddedToView) {
				mHostGroup.removeElement(mComponentGroup);
			}
			mHostGroup = null;
			mAddedToView = false;
		}
	}

	abstract protected void updateView();

	abstract public double getLength();

	abstract public double getWidth();

	abstract public double getHigh();

}
