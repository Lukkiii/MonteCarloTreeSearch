(define (domain taquin)
  (:requirements :strips :typing)
  (:types tile position)
  (:predicates
    (at ?t - tile ?p - position)
    (empty ?p - position)
    (adjacent ?p1 ?p2 - position)
  )

  (:action move
    :parameters(?t - tile ?from ?to - position)
    :precondition(and (empty ?to)
                      (adjacent ?from ?to)
                      (at ?t ?from))
    :effect(and (empty ?from)
                (not(empty ?to))
                (at ?t ?to)
                (not(at ?t ?from)))
  )

)