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
}
