#!ruby

prefix = ARGV[0]
File.open ARGV[1] do |ah|
	File.open ARGV[2] do |bh|
		i = 0
		ncats = ah.gets.to_i
		ncats == bh.gets.to_i or die "bad input"
		catns = []
		catIx = {}
		mtx = (0...ncats).map do |j|
			cat = ah.gets
			catIx[cat] = j
			catns[j] = cat.chomp
			cat == bh.gets or die "bad input"
			(0...ncats).map { 0 }
		end
		num = ah.gets.to_i
		num == bh.gets.to_i or die "bad input"
		while a = ah.gets and b = bh.gets
			mtx[catIx[a.sub /\s.*/, ""]][catIx[b.sub /\s.*/, ""]] += 1
		end
		puts "#{prefix % (i += 1)} o|#{catns.join " o|"} observed/expected"
		(0...ncats).each do |j|
			puts "#{prefix % (i += 1)} #{mtx[j].join " "} e|#{catns[j]}"
		end
	end
end
