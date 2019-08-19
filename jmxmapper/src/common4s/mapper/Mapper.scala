package common4s.mapper

/**
 * @author Kai Han
 */
trait Mapper[T, R] {
	def map(t : T) : R
}