package nl.esi.almarvi.buffersizing;

/**
 * An Oracle encapsulates a computational process (discrete-event simulation model, ...) that for a given
 * Point (parameters of the process, e.g., buffer sizes) determines whether the constraints (e.g., throughput
 * larger than some threshold) are satisfied.
 */
public interface Oracle {

    OracleResult eval(Point p);
}
