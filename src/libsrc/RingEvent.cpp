

#include <cstdlib>
#include "RingEvent.h"


namespace ersap {

    /** Function used to create (shared pointers to) RingEvent objects by a RingBuffer. */
    const std::function< std::shared_ptr<RingEvent> () >& RingEvent::eventFactory() {
        static std::function< std::shared_ptr<RingEvent> () > result([] {
            return std::move(std::make_shared<RingEvent>());
        });
        return result;
    }


}

