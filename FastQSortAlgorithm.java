/*
 * @(#)QSortAlgorithm.java	1.3   29 Feb 1996 James Gosling
 *
 * Copyright (c) 1994-1996 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Permission to use, copy, modify, and distribute this software
 * and its documentation for NON-COMMERCIAL or COMMERCIAL purposes and
 * without fee is hereby granted.
 * Please refer to the file http://www.javasoft.com/copy_trademarks.html
 * for further important copyright and trademark information and to
 * http://www.javasoft.com/licensing.html for further important
 * licensing information for the Java (tm) Technology.
 */
package jade.realtime.scheduler;

import java.util.ArrayList;
import java.util.StringTokenizer;
import queueUtils.maIdent;


public class FastQSortAlgorithm
{
	/** This is a generic version of C.A.R Hoare's Quick Sort
	* algorithm.  This will handle arrays that are already
	* sorted, and arrays with duplicate keys.<BR>
	*
	* If you think of a one dimensional array as going from
	* the lowest index on the left to the highest index on the right
	* then the parameters to this function are lowest index or
	* left and highest index or right.  The first time you call
	* this function it will be with the parameters 0, a.length - 1.
	*
	* @param a	   an integer array
	* @param lo0	 left boundary of array partition
	* @param hi0	 right boundary of array partition
	*/

	//private ArrayList<Integer> index = new ArrayList<Integer>();
	private ArrayList<Integer> a = new ArrayList<Integer>();
	private ArrayList<maIdent> cv = new ArrayList <maIdent>();

	private void QuickSort(ArrayList<Integer> a, int l, int r) throws Exception
   {
	int M = 4;
	int i;
	int j;
	int v;

	if ((r-l)>M)
	{
		i = (r+l)/2;
		if (a.get(l)>a.get(i)) { swap(a,l,i); swapAID(cv,l,i); } //swap(index,l,i); }	// Tri-Median Methode!
		if (a.get(l)>a.get(r)) { swap(a,l,r); swapAID(cv,l,r); }//swap(index,l,r); }
		if (a.get(i)>a.get(r)) { swap(a,i,r); swapAID(cv,i,r); }//swap(index,i,r); }

		j = r-1;
		swap(a,i,j);
		swapAID(cv,i,j);
	//	swap(index,i,j);
		i = l;
		v = a.get(j);
		for(;;)
		{
			while(a.get(++i)<v);
			while(a.get(--j)>v);
			if (j<i) break;
			swap (a,i,j);
			swapAID (cv,i,j);
		//	swap (index,i,j);
		}
		swap(a,i,r-1);
		swapAID(cv,i,r-1);
	//	swap(index,i,r-1);
		QuickSort(a,l,j);
		QuickSort(a,i+1,r);
	}
}

	private void swapAID(ArrayList<maIdent> a, int i, int j)
	{
		maIdent T;
		T = a.get(i);
		a.set(i,a.get(j));//a[i] = j

		a.set(j,T);

	}


	private void swap(ArrayList<Integer> a, int i, int j)
	{
		int T;
		T = a.get(i);
		a.set(i,a.get(j));//a[i] = j

		a.set(j,T);

	}

	private void InsertionSort(ArrayList<Integer> a, int lo0, int hi0) throws Exception
	{
		int i;
		int j;
		int v;
		maIdent p;
	//	int k;

		for (i=lo0+1;i<=hi0;i++)
		{
			v = a.get(i);
			p = cv.get(i);
		//	k = index.get(i);
			j=i;
			while ((j>lo0) && (a.get(j-1)>v))
			{
				a.set(j,a.get(j-1));
				cv.set(j,cv.get(j-1));
				//index.set(j,index.get(j-1));
				j--;
			}
			a.set(j,v);
			cv.set(j,p);
		//	index.set(j,k);

	 	}
	}

	public ArrayList<maIdent> schedule(ArrayList<String> b,ArrayList<maIdent> cv) throws Exception
	{
		this.cv = cv;

		StringTokenizer comparator;
		for(int k=0;k<b.size();k++){
			comparator = new StringTokenizer(b.get(k),":");
			while(comparator.hasMoreTokens()){
			//	index.add(Integer.parseInt(comparator.nextToken()));
				comparator.nextToken();
				a.add(Integer.parseInt(comparator.nextToken()));
			}
		}
		QuickSort(a, 0, a.size()- 1);
		InsertionSort(a,0,a.size()-1);
	/*	b.clear();
		for(int i=0;i<a.size();i++){
			b.add(index.get(i)+":"+a.get(i));
		}	 */
		return this.cv;

	}
}

