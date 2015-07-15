
#include <iostream>
using namespace std;

class HashEntry {
public:
  long long key;
  int score;

  HashEntry(long k, int s){
    key = k;
    score = s;
  }
};

int main(){
  cout << sizeof(HashEntry);
  return(0);
}



