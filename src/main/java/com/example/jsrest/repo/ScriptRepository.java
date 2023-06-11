package com.example.jsrest.repo;

import com.example.jsrest.model.Script;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScriptRepository extends CrudRepository<Script, Long>, PagingAndSortingRepository<Script, Long>
{
    Iterable<Script> findByStatus(String status, Sort sort);
}
