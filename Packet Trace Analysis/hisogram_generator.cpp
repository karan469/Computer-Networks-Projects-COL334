#include <iostream>
#include <bits/stdc++.h>
#include <fstream>
#include <vector>
#include <string>

using namespace std;

vector<string> giveTokens(string s){
	vector<string> res;
	string delimiter = ",";
	size_t pos = 0;
	std::string token;
	int count = 0;
	while ((pos = s.find(delimiter)) != std::string::npos && count<6) {
		 token = s.substr(0, pos);
		 count++;
		 res.push_back(token);
		 s.erase(0, pos + delimiter.length());
	}
	res.push_back(s);
	return res;
}

void trafficCount(string filename){
	ifstream myfile(filename);
	string id = filename.substr(13,filename.length()-37);
	ofstream TCP_traffic_histogram;

	vector<int> count(23,0);
	if(myfile.is_open() && filename.find("_unique_flow_ackonly") != std::string::npos){
		TCP_traffic_histogram.open("./outputs/q3/" + id + "_tcp_histogram.csv");
		string l;
		while(getline(myfile,l)){
			vector<string> line = giveTokens(l);
			int time = stoi(line[1].substr(1,line[1].length()-2));
			// cout<<time<<endl;
			count[time/3600] += 1;
		}

		// cout<<"----------New Histogram Output-----------"<<endl;
		TCP_traffic_histogram<<"\"Time Interval\",\"No. of Connections\""<<endl;
		for(int i=0;i<24;i++){
			TCP_traffic_histogram<<"\""<< i <<"\""<<","<<"\""<<count[i]<<"\""<<endl;
			// Uncomment below line to debug histograms
			// cout<<"TCP Traffic in "<<i<<" Hour: "<<count[i]<<endl;
		
		}
		cout<<"File written successfully: " + id + "_tcp_histogram.csv"<<endl<<endl;
		TCP_traffic_histogram.close();
	} else {
		cout<<"File not found or corrupted: "<< filename <<" | Exiting..."<<endl<<endl;
	}
}

int main(int argc, char const *argv[])
{
	vector<string> s = {{"lbnl.anon-ftp.03-01-11"},{"lbnl.anon-ftp.03-01-14"},{"lbnl.anon-ftp.03-01-18"}}; 
	for(int i=0;i<s.size();i++){trafficCount("./outputs/q2/" + s[i] + "_unique_flow_ackonly.csv");}
	return 0;
}
