//
// Created by Carl Timmer on 2/11/21.
//

#ifndef ERSAP_VTP_CHARGETIME_H
#define ERSAP_VTP_CHARGETIME_H

#include <cstdlib>
#include <string>


namespace ersap {

    class ChargeTime {

    private:

        int64_t time;
        int32_t charge;

    public:

        ChargeTime(int64_t time, int32_t charge) {
            this->time = time;
            this->charge = charge;
        }

        int64_t getTime()   const {return time;}
        int32_t getCharge() const {return charge;}


        bool operator == (const ChargeTime & rhs) const {
            if (this == &rhs) return true;
            return ((getCharge() == rhs.getCharge()) && (getTime() == rhs.getTime()));
        }

        std::string toString() const {
            std::stringstream ss;
            ss << "ChargeTime{time=" << time << ", charge=" << charge << '}';
            return ss.str();
        }
    };

}


#endif //ERSAP_VTP_CHARGETIME_H
