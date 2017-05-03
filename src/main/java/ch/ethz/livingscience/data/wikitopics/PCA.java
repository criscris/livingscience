package ch.ethz.livingscience.data.wikitopics;

import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.NotConvergedException;
import no.uib.cipr.matrix.SVD;

/**

[n,p] = size(x); % n variables, p observations

% Center X by subtracting off column means
x0 = bsxfun(@minus,x,mean(x,1));
% x0 is [n,p]

% [V,D] = eig(A) produces matrices of eigenvalues (D) and eigenvectors (V) of matrix A, so that A*V = V*D
[coeff,ignore] = eig(x0'*x0);
%  B = fliplr(A) returns A with columns flipped in the left-right direction, that is, about a vertical axis (einfach gespiegelt)
coeff = fliplr(coeff);

% == [U,S,V] = svd(x0'*x0);, V muss nicht mehr geflippt werden (alles ist aber *-1, sonst wie coeff

 */

public class PCA
{
	int numOfObservations;
	DenseMatrix Vt;
	
	/**
	 * pc = princomp(data');
	 * plot(pc(:,1)*-1, pc(:,2), 'x');
	 * 
	 * and
	 * # pca.getPrincipalComponent(x, 0);
	 * # pca.getPrincipalComponent(y, 1);
	 * plot(x, y, 'ro');
	 * 
	 * shall match.
	 * 
	 * @param data [observations][variables]
	 */
	public PCA(double[][] data)
	{
		numOfObservations = data.length;
		int numOfVariables = data[0].length;
		
		meanCenterRows(data);
		Matrix X = new DenseMatrix(data); // copies data unnecessarily
		Matrix XT = new DenseMatrix(numOfVariables, numOfObservations);
		X.transpose(XT);
		
		DenseMatrix A = new DenseMatrix(numOfObservations, numOfObservations);
		X.mult(XT, A);
		
		SVD svd = new SVD(numOfObservations, numOfObservations, true);
		try 
		{
			svd.factor(A);
		} 
		catch (NotConvergedException e) 
		{
			e.printStackTrace();
		}
		
		Vt = svd.getVt();
		
		double[] d = Vt.getData();
		
		// output is transposed, i.e. V
//		System.out.print("PC = [");
//		for (int o=0; o<numOfObservations; o++)
//		{
//			for (int v=0; v<numOfObservations; v++)
//			{
//				System.out.print(d[o*numOfObservations + v]);
//				if (v < numOfObservations - 1) System.out.print(", ");
//			}
//			
//			if (o < numOfObservations - 1) System.out.println(";");
//		}
//		System.out.println("];");
		

	}
	
	public void meanCenterRows(double data[][])
	{
		for (int i=0; i<data.length; i++)
		{
			double[] row = data[i];
			
			double sum = 0;
			for (int j=0; j<row.length; j++)
			{
				sum += row[j];
			}
			double mean = sum / row.length;
			
			for (int j=0; j<row.length; j++)
			{
				row[j] -= mean;
			}
		}
	}
	
	/**
	 * 
	 * @param target
	 * @param component 0 for 1st component, 1 for 2nd...
	 */
	public void getPrincipalComponent(double[] target, int component)
	{
		for (int i=0; i<numOfObservations; i++)
		{
			target[i] = Vt.get(component, i); // changed row/column since we actually need V not Vt
		}
	}
}
