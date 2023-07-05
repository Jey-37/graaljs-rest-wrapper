package com.example.jsrest.repo;

import com.example.jsrest.model.Script;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScriptRepository extends CrudRepository<Script, Long>, PagingAndSortingRepository<Script, Long>
{
    Iterable<Script> findByStatus(Script.ScriptStatus status, Sort sort);

    @Modifying
    @Query("update Script s set s.output = concat(s.output, :output) where s.id = :id")
    void appendOutput(long id, String output);
}
