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

	// std::stringstream ss(s);
	// for (string i; ss >> i;) {
	// 	if(count<6) {res.push_back(i);count++;}	
	// 	if (ss.peek() == ',')
	// 		ss.ignore();
	// }
	return res;
}

void connectionDuration(string filename){
	ifstream myfile(filename);
	if(myfile.is_open() && filename.find("_letsee_all") != std::string::npos){
		// cout<<"OPENED " + filename<<endl;
		
	} else {
		cout<<"File " + filename + " not opened correctly."<<endl;
	}
}

int main(int argc, char const *argv[])
{
	string path_header = "./outputs/q4/";
	vector<string> s = {{"lbnl.anon-ftp.03-01-11"},{"lbnl.anon-ftp.03-01-14"},{"lbnl.anon-ftp.03-01-18"}}; 
	for(int i=0;i<1;i++){connectionDuration(path_header + s[i] + "_letsee_all.csv");}

	return 0;
}