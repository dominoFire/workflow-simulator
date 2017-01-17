
# Comparacion de distribucion
density_compare = function(algo_data, var_name, title_graph)
  with(as.data.frame(algo_data), {
    colfill = 1:length(levels(algo_name)) + 1
    algo_vector = algo_data[, var_name]
    kde_makespan = sm::sm.density.compare(algo_vector, algo_name, lty=2)
    legend(list(x=20.11, y=0.074), levels(algo_name), fill=colfill)
    title(main = title_graph)
  }) 

density_compare_ggplot = function(algo_data, var_name, title, bin_width = 0.5) {
  graph_expr = substitute(
    ggplot2::ggplot(algo_shape) +
      ggplot2::geom_freqpoly(aes(x = var_name, 
                                 y = ..density.., 
                                 fill = algo_name, 
                                 colour = algo_name), binwidth = bin_width))
  eval(graph_expr)
}


tidy_logical = function(x) x %>% toupper() %>% as.logical()
