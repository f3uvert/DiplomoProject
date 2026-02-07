package ru.practicum.entity;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "compilation_events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompilationEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compilation_id", nullable = false)
    private Compilation compilation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;
}