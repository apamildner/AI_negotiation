package group39;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import genius.core.Domain;
import genius.core.boaframework.NegotiationSession;
import genius.core.issue.Issue;
import genius.core.issue.Value;
import genius.core.issue.ValueDiscrete;
import genius.core.uncertainty.AdditiveUtilitySpaceFactory;
import genius.core.uncertainty.UserModel;
import genius.core.utility.AbstractUtilitySpace;

public class Uncertainty_Helper2 {
	NegotiationSession session;
	UserModel userModel;
	
	
	public Uncertainty_Helper2(NegotiationSession session) {
		this.session = session;
		this.userModel = session.getUserModel();
	}
	
	
	
	
	/**
	 * With this method, you can override the default estimate of the utility
	 * space given uncertain preferences specified by the user model. This
	 * example sets every value to zero.
	 */
	public AbstractUtilitySpace estimateUtilitySpace() {
		
		Domain domain = session.getDomain();
		AdditiveUtilitySpaceFactory factory = new AdditiveUtilitySpaceFactory(domain);
						
		HashMap<Integer,Double> IssueWeights = generateIssueWeights(domain);
		HashMap<Integer,HashMap<ValueDiscrete,Integer>> ValueWeights = generateValueWeights(domain);
		
		//Set issue weights
		int nrIssues = IssueWeights.size();
		List<Issue> Issues = new ArrayList<>();
		double[] IssueWeightsNotNormalized = new double[nrIssues];
		int idx1=0;
		for(Issue issue : domain.getIssues()) {
			
			
			
			Issues.add(issue);
			int issueNr=issue.getNumber();
			IssueWeightsNotNormalized[idx1]=IssueWeights.get(issueNr);
			for(ValueDiscrete issueValue : ValueWeights.get(issueNr).keySet()) {
				int w= ValueWeights.get(issueNr).get(issueValue);
				factory.setUtility(issue, issueValue, w);
			}
			idx1++;
		}
		double[] IssueWeightsNormalized=divide(IssueWeightsNotNormalized,sumVector(IssueWeightsNotNormalized));
		
		factory.getUtilitySpace().setWeights(Issues, IssueWeightsNormalized);
		System.out.println(factory.getUtilitySpace());
		
		return factory.getUtilitySpace();
	}
	
	private double[] divide(double[] d, double n) {
		for(int i=0; i<d.length;i++) {
			d[i]=d[i]/n;
		}
		return d;
	}
	
	private double sumVector(double[] d) {
		double s=0;
		for(int i=0;i<d.length;i++) {
			s=s+d[i];
		}
		return s;
	}
	
	
	private HashMap<Integer, Double> generateIssueWeights(Domain domain){
		//Parameters that 
		double a=1;
		double b=2;
		System.out.println("Generating weights");
		
		HashMap<Integer, Double> IssueWeights = new HashMap<Integer,Double>();
		System.out.println(this.userModel);
		int nrBids = this.userModel.getBidRanking().getSize();	
		for(int i=0;i<nrBids-1;i++) {
			try {
				//Decreasing added issue weight
				double w=(b-(b-a)*(i/nrBids));
				
				for(Issue issue : domain.getIssues()) {
					Value v1= userModel.getBidRanking().getBidOrder().get(nrBids-1-i).getValue(issue.getNumber());
					Value v2= userModel.getBidRanking().getBidOrder().get(nrBids-2-i).getValue(issue.getNumber());
	//				
					//add weight if two issues are the same
					if(v1.equals(v2)) {
						if(IssueWeights.containsKey(issue.getNumber())) {
							double c=IssueWeights.get(issue.getNumber())+w;
							IssueWeights.replace(issue.getNumber(), c);
						}else {
							IssueWeights.put(issue.getNumber(), w);
						}
					}
				}
			}catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return IssueWeights;
	}
	
	private HashMap<Integer, HashMap<ValueDiscrete, Integer>> generateValueWeights(Domain domain){
		HashMap<Integer, HashMap<ValueDiscrete, Integer>> AllValueWeights = new HashMap<Integer,HashMap<ValueDiscrete, Integer>>();
		int nrBids = userModel.getBidRanking().getSize();
		
		//parameters to experiment with
		int b=3; //first value
		int a=1; //should a decrease linearly? from b to a
		
		for(int i=0;i<nrBids;i++) {
			try {
				
				int w=Math.round((b-(b-a)*(i/nrBids)));
				
				for(Issue issue : domain.getIssues()) {
					int issueNr = issue.getNumber();
					ValueDiscrete v= (ValueDiscrete) userModel.getBidRanking().getBidOrder().get(nrBids-1-i).getValue(issue.getNumber());
					int bonus = 0;
					if (i>2 && (i<nrBids-2)) {
						ValueDiscrete v1= (ValueDiscrete) userModel.getBidRanking().getBidOrder().get(nrBids-i+1).getValue(issue.getNumber());
//						ValueDiscrete v2= (ValueDiscrete) userModel.getBidRanking().getBidOrder().get(nrBids-i+2).getValue(issue.getNumber());
						if (v.getValue().equals(v1.getValue())) {
							bonus = 3;
						}
					}
					if(AllValueWeights.containsKey(issueNr)) {
						if(AllValueWeights.get(issueNr).containsKey(v)) {
							int c=AllValueWeights.get(issueNr).get(v)+w+bonus;
							AllValueWeights.get(issueNr).replace(v, c);
						}else {
							AllValueWeights.get(issueNr).put(v,b);
						}
					}else {
						HashMap<ValueDiscrete, Integer> ValueWeights = new HashMap<ValueDiscrete, Integer>();
						ValueWeights.put(v, w);
						AllValueWeights.put(issueNr, ValueWeights);
					}
				}
			}catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return AllValueWeights;
	}

}
