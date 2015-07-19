package it.unitn.studenti.alessiobogon.concurrency.nbbst;

/**
 * Created by Alessio Bogon on 11/06/15.
 */
class Update {
    final State state;
    final Info info;

    Update(State state, Info info){
        this.state = state;
        this.info = info;
    }

    @Override
    public String toString() {
        return "Update(" + state + ", " + info + ")";
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Update){
            Update ot = (Update) other;
            return this.state == ot.state && this.info == ot.info;
        }
        return false;
    }
}
