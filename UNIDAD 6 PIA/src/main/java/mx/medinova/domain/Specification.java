package mx.medinova.domain;
import java.util.Objects;
import java.util.function.Predicate;
/** Specification pattern; composable with AND, OR and NOT. */
public interface Specification<T> extends Predicate<T> {
 default Specification<T> andSpec(Specification<T> other){ Objects.requireNonNull(other); return t -> test(t) && other.test(t); }
 default Specification<T> orSpec(Specification<T> other){ Objects.requireNonNull(other); return t -> test(t) || other.test(t); }
 default Specification<T> not(){ return t -> !test(t); }
}
